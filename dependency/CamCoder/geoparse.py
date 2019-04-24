import cPickle
import codecs
import sqlite3
import sys
from genericpath import isfile
from os import listdir
import spacy
import numpy as np
from geopy.distance import great_circle
from keras.models import load_model
from preprocessing import index_to_coord, ENCODING_MAP_1x1, OUTLIERS_MAP_1x1, get_coordinates
from preprocessing import CONTEXT_LENGTH, pad_list, TARGET_LENGTH, UNKNOWN, REVERSE_MAP_2x2
from text2mapVec import text2mapvec




#  for word in nlp.Defaults.stop_words:  # This is only necessary if you use the full Spacy English model
#     lex = nlp.vocab[word]             # so if you use spacy.load(u'en'), you can comment this out.
#     lex.is_stop = True


def geoparse(text, result_string, model, word_to_index):
    """
    This function allows one to geoparse text i.e. extract toponyms (place names) and disambiguate to coordinates.
    :param text: to be parsed
    :return: currently only prints results to the screen, feel free to modify to your task
    """
    nlp = spacy.load(u'en')  # or spacy.load(u'en') depending on your Spacy Download (simple or full)
    conn = sqlite3.connect(u'/opt/gsda/EUPEG/Geoparsers/camCoder/data/geonames.db').cursor()  # this DB can be downloaded using the GitHub link
    padding = nlp(u"0")[0]  # Do I need to explain? :-)

    doc = nlp(text)  # NER with Spacy NER
    for entity in doc.ents:
        if entity.label_ in [u"GPE", u"FACILITY", u"LOC", u"FAC", u"LOCATION"]:
            name = entity.text if not entity.text.startswith('the') else entity.text[4:].strip()
            start = entity.start_char if not entity.text.startswith('the') else entity.start_char + 4
            end = entity.end_char
            near_inp = pad_list(CONTEXT_LENGTH / 2, [x for x in doc[max(0, entity.start - CONTEXT_LENGTH / 2):entity.start]], True, padding) + \
                       pad_list(CONTEXT_LENGTH / 2, [x for x in doc[entity.end: entity.end + CONTEXT_LENGTH / 2]], False, padding)
            far_inp = pad_list(CONTEXT_LENGTH / 2, [x for x in doc[max(0, entity.start - CONTEXT_LENGTH):max(0, entity.start - CONTEXT_LENGTH / 2)]], True, padding) + \
                      pad_list(CONTEXT_LENGTH / 2, [x for x in doc[entity.end + CONTEXT_LENGTH / 2: entity.end + CONTEXT_LENGTH]], False, padding)
            map_vector = text2mapvec(doc=near_inp + far_inp, mapping=ENCODING_MAP_1x1, outliers=OUTLIERS_MAP_1x1, polygon_size=1, db=conn, exclude=name)

            context_words, entities_strings = [], []
            target_string = pad_list(TARGET_LENGTH, [x.text.lower() for x in entity], True, u'0')
            target_string = [word_to_index[x] if x in word_to_index else word_to_index[UNKNOWN] for x in target_string]
            for words in [near_inp, far_inp]:
                for word in words:
                    if word.text.lower() in word_to_index:
                        vec = word_to_index[word.text.lower()]
                    else:
                        vec = word_to_index[UNKNOWN]
                    if word.ent_type_ in [u"GPE", u"FACILITY", u"LOC", u"FAC", u"LOCATION"]:
                        entities_strings.append(vec)
                        context_words.append(word_to_index[u'0'])
                    elif word.is_alpha and not word.is_stop:
                        context_words.append(vec)
                        entities_strings.append(word_to_index[u'0'])
                    else:
                        context_words.append(word_to_index[u'0'])
                        entities_strings.append(word_to_index[u'0'])

            prediction = model.predict([np.array([context_words]), np.array([context_words]), np.array([entities_strings]),
                                        np.array([entities_strings]), np.array([map_vector]), np.array([target_string])])
            prediction = index_to_coord(REVERSE_MAP_2x2[np.argmax(prediction[0])], 2)
            candidates = get_coordinates(conn, name)

            if len(candidates) == 0:
                # print(u"Don't have an entry for", name, u"in GeoNames")
                continue

            max_pop = candidates[0][2]
            best_candidate = []
            bias = 0.905  # Tweak the parameter depending on the domain you're working with.
            # Less than 0.9 suitable for ambiguous text, more than 0.9 suitable for less ambiguous locations, see paper
            for candidate in candidates:
                err = great_circle(prediction, (float(candidate[0]), float(candidate[1]))).km
                best_candidate.append((err - (err * max(1, candidate[2]) / max(1, max_pop)) * bias, (float(candidate[0]), float(candidate[1]))))
            best_candidate = sorted(best_candidate, key=lambda (a, b): a)[0]

            # England,, England,, 51.5,, -0.11,, 669,, 676 || - use evaluation script to test correctness
            # print name, start, end

            # print u"Coordinates:", best_candidate[1]
            name = name.encode('utf-8')
            one_toponym = "{0},,{1},,{2},,{3},,{4},,{5}||".format(name, name, best_candidate[1][0], best_candidate[1][1], start,  end)
            result_string = result_string + one_toponym

    return result_string


def main(argv1, argv2):
    # load necessary configuration
    # argv1 -- output file path
    # argv2 --  corpus path

    p_model = load_model("/opt/gsda/EUPEG/Geoparsers/camCoder/data/weights")  # weights to be downloaded from Cambridge Uni repo, see GitHub.
    p_word_to_index = cPickle.load(open(u"/opt/gsda/EUPEG/Geoparsers/camCoder/codes/data/words2index.pkl"))  # This is the vocabulary file
    # Example usage of the geoparse function below reading from a directory and parsing all files.
    directory = unicode(str(argv2), 'utf-8')
    files = [f for f in listdir(directory) if isfile(directory + f)]
    # output_file = open(argv1, 'w+')
    with open(argv1, "w") as wf:
        for i in range(0, len(files)):
            # print i
            full_line = ""
            for line in codecs.open(directory + str(i), encoding="utf-8", errors='ignore'):
                full_line = full_line + line
            article_parsing = geoparse(full_line, "", p_model, p_word_to_index)
            # write result into the output
            wf.write(article_parsing)
            wf.write('\n')


if __name__ == "__main__":
    output = sys.argv[2]
    corpus_path = sys.argv[1]
    main(argv1=output, argv2=corpus_path)
    print "successful"



# results = geoparse(line, [], p_model, p_word_to_index)