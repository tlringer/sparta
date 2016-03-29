#!/bin/bash

#Receives as argument the root directory of the target app containing the
#AndroidManifest.xml file, followed by  a set of apps
#that might interact with it. Generates a component-map for the target app.

if [ -z "$1" ]
    then
	    echo "No argument supplied"
		exit 0
fi

function downloadJars {
    mkdir download-libs
    cd download-libs
	mkdir epicc
	mkdir dare

#Dare.jar
	echo Downloading Dare
	echo ===========================================
	if [[ "$OSTYPE" == "linux-gnu" ]]; then
		curl -o dare.tgz -L http://siis.cse.psu.edu/dare/downloads/dare-1.0.2_2-linux.tgz
		tar zxvf dare.tgz -C dare/
		cp -R dare/dare-1.0.2_2-linux/. dare/ 
		rm -rf dare/dare-1.0.2_2-linux/*
		rmdir dare/dare-1.0.2_2-linux
	elif [[ "$OSTYPE" == "darwin"* ]]; then
		curl -o dare.tgz -L http://siis.cse.psu.edu/dare/downloads/dare-1.0.2_2-macos.tgz
		tar zxvf dare.tgz -C dare/
		cp -R dare/dare-1.0.2_2-macos/. dare/
		rm -rf dare/dare-1.0.2_2-macos/*
		rmdir dare/dare-1.0.2_2-macos
	else
    	echo "This script only runs in linux or mac OS"
    	exit 0
	fi
	echo ===========================================
#Epicc.jar
    echo Downloading Epicc
	echo ===========================================
	curl http://siis.cse.psu.edu/epicc/downloads/epicc-0.1.tgz -o epicc.tgz
    echo ===========================================
    tar zxvf epicc.tgz -C epicc/
#APKParser.jar
	echo Downloading APKParser
	echo ===========================================
	curl https://xml-apk-parser.googlecode.com/files/APKParser.jar -o APKParser.jar
    echo ===========================================
	cd ..

}

function generateFilters {
cd $SPARTA_CODE
rm -f $FILTERSMAP
cp $FILTERSMAPBASE $FILTERSMAP
for apkFile
do
    CURRENTAPKPATH=$(cd $(dirname $apkFile); pwd)/$(basename $apkFile)
    if [[ "$CURRENTAPKPATH" == *\.apk ]]
    then
        echo Adding "$apkFile" information into filter-map
        java -jar ./download-libs/APKParser.jar "$CURRENTAPKPATH" > AndroidManifestTemp.xml
        java -cp build/ sparta.checkers.intents.componentmap.ProcessAndroidManifest AndroidManifestTemp.xml "$FILTERSMAP"
         rm -f AndroidManifestTemp.xml
    else
        echo File passed as input is not an .apk file: "$apkFile"
    fi
done
}

#Building SPARTA
cd $SPARTA_CODE
if [ ! -d ./build ]; then
    ant
fi

#Building target app
APP_DIR=$1
cd $APP_DIR
#FINDMANIFEST='find `pwd` -name 'AndroidManifest.xml' | head -1'
#MANIFESTFOLDER=$(dirname $(eval $FINDMANIFEST))
#cd $MANIFESTFOLDER
mkdir -p libs
rm -f libs/sparta.jar
#cp $SPARTA_CODE/sparta.jar libs/sparta.jar
./gradlew build

#Target app's apk path
FINDAPK='find `pwd` -name '*-release-unsigned.apk' | head -1'
APKPATH=$(eval $FINDAPK)

#Output directory
OUTDIR="$APP_DIR"/sparta-out/
#Filter-map with android built-in components
FILTERSMAPBASE="$SPARTA_CODE"/src/sparta/checkers/intents/componentmap/filter-map-base
#Filter-map with android built-in components and all apps passed as argument
FILTERSMAP="$OUTDIR"/filter-map
rm -rf "$OUTDIR"
mkdir -p "$OUTDIR"
#Output path of the component-map
CMPATH="$OUTDIR"/component-map
#Path to epicc's output
EPICCOUTPUT="$OUTDIR"/epiccoutput.txt

TARGETFOLDER_WITH_EXTENSION=$(basename $APKPATH)
TARGETFOLDER=${TARGETFOLDER_WITH_EXTENSION%.apk}
#Dare's output folder, used by epicc.
RETARGETEDPATH=./download-libs/epicc/retargeted/"$TARGETFOLDER"

cd $SPARTA_CODE
if [ ! -f ./download-libs/APKParser.jar ]; then
    downloadJars
fi

generateFilters "$APKPATH" "${@:2}"

#Using DARE
./download-libs/dare/dare -d ./download-libs/epicc/ "$APKPATH"

echo "Using Epicc"
echo "java -Xmx6g -jar ./download-libs/epicc/epicc-0.1.jar -apk" "$APKPATH" "-android-directory" "$RETARGETEDPATH" "-cp ./download-libs/epicc/android.jar icc-study" "$OUTDIR" ">" "$EPICCOUTPUT"

#Using Epicc
java -Xmx6g -jar ./download-libs/epicc/epicc-0.1.jar -apk "$APKPATH" -android-directory "$RETARGETEDPATH" -cp ./download-libs/epicc/android.jar icc-study "$OUTDIR" > "$EPICCOUTPUT"
#Epicc output generated. Removing unnecessary data.
rm -rf ./download-libs/epicc/retargeted/*
rm -rf ./download-libs/epicc/optmized/*
rm -rf ./download-libs/epicc/optimized-decompiled/*

echo "Processing epicc output"
echo "java -Xmx1024M -cp build/ sparta.checkers.intents.componentmap.ProcessEpicOutput" "$EPICCOUTPUT" "$FILTERSMAP" "$CMPATH"

#Processing epicc output with filters
java -Xmx1024M -cp build/ sparta.checkers.intents.componentmap.ProcessEpicOutput "$EPICCOUTPUT" "$FILTERSMAP" "$CMPATH"
