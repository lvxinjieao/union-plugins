import file_utils
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
import sdk_helper


androidNS = 'http://schemas.android.com/apk/res/android'


def modifyPackageName(game, channel, decompileDir, packageName):

    manifestFile = decompileDir + "/AndroidManifest.xml"
    manifestFile = file_utils.getFullPath(manifestFile)
    ET.register_namespace('android', androidNS)  
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    oldPacakage = 'com.xuancai.museum'

    key = '{'+androidNS+'}authorities'
    nameKey = '{'+androidNS+'}name'

    appNode = root.find('application')
    if appNode is None:
        return

    providerNodes = appNode.findall('provider')

    for pNode in providerNodes:

        name = pNode.get(nameKey)
        if name == 'com.u8.sdk.GameFileProvider':
            pNode.set(key, packageName+".fileProvider")

    tree.write(manifestFile, 'UTF-8')



def execute(channel, decompileDir, packageName):

    activityName = sdk_helper.removeStartActivity(decompileDir, 'com.u8.sdk.QuickSplashActivity')

    smaliPath = os.path.join(decompileDir, 'smali/com/u8/sdk/QuickSplashActivity.smali')

    file_utils.modifyFileContent(smaliPath, "${Game_Launch_Activity}", activityName)

    sdk_helper.modifyRootApplicationExtends(decompileDir, 'com.quicksdk.QuickSdkApplication')




    return 0
