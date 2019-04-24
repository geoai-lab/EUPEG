# -*- coding: utf-8 -*-

import spacy
import json
import sys
import re


def main(argv):
	p_str = unicode(str(argv), 'utf-8')
	spacy_relevant_entities_en = ['GPE','LOC','ORG','FACILITY']
	spacy_relevant_entities_xx = ['LOC']
	nlp = spacy.load('en')
	doc = nlp(p_str)
	spacy_toponym_array = []
	for ent in doc.ents:
		ent_label = ent.label_
		spacy_token_array = []
		if ent_label in spacy_relevant_entities_en:
			if len(ent.text) < 3:
				continue
			elif not re.search('[a-zA-Z]', ent.text):
				continue
			else:
				ent_text = (ent.text.replace('\n', "")).strip()
				spacy_token_array.append(ent_text)
				spacy_token_array.append(ent.start_char)
				spacy_token_array.append(ent.end_char)
				spacy_toponym_array.append(spacy_token_array)
				# print(ent.text, ent.start_char, ent.end_char, ent.label_)

	json_result = json.dumps(spacy_toponym_array)
	print json_result


if __name__ == "__main__":
	main(sys.argv[1])
