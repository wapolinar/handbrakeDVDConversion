#!/bin/bash
# Convert iso files using handbrake
# 28.08.2015 Wolfgang Apolinarski

filesToConvert=()
while read -r -d ''; do
	filesToConvert+=("$REPLY")
done < <(find . -type f | grep -i \.iso | tr "\n" "\0") 

for file in "${filesToConvert[@]}";
do
#	echo $file
	java -jar dvd_conversion.jar "$file"
done
