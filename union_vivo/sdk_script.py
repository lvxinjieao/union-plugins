import file_utils
import sdk_helper
import os
import os.path
import config_utils
from xml.etree import ElementTree as ET
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import ElementTree
import os
import os.path
import zipfile
import re
import subprocess
import platform
from xml.dom import minidom
import codecs
import sys
import json

androidNS = 'http://schemas.android.com/apk/res/android'


def execute(channel, decompileDir, packageName):

	appID = sdk_helper.getSdkParamByKey(channel, 'Vivo_AppID')

	supplierconfig = os.path.join(decompileDir, 'assets/supplierconfig.json')

	config = None
	if config_utils.is_py_env_2():
		with open(supplierconfig, 'r') as f:
			content = f.read()
			config = json.loads(content)
	else:
		with open(supplierconfig, 'r', encoding="utf-8") as f:
			content = f.read()
			config = json.loads(content)

	config['supplier']['vivo']['appid'] = appID

	if config_utils.is_py_env_2():
		with open(supplierconfig, 'w') as f:
			f.write(json.dumps(config))
	else:
		with open(supplierconfig, 'w', encoding="utf-8") as f:
			f.write(json.dumps(config))

	return 0

