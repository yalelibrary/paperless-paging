#!/bin/python
import json
import sys
data = json.load(sys.stdin)
print(json.loads(data['SecretString'])[sys.argv[1]])