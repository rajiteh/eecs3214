#!/bin/sh

if [ "$#" -ne 3 ]
then
	echo "Usage <file_size_in_kb> <file_name> <string>"
	exit 1
fi

max_size=`expr $1 \* 1000`
filename=$2
tmpfilename=`mktemp`
string="$3"
 
printf "$string" > $filename
 
# Initialize the variable
size=$(stat -c %s $filename)
 
# Start the loop, increasing the size of the file 2x until reaching max_size
while [ $size -lt $max_size ]; do
        cat $filename > $tmpfilename
        cat $tmpfilename >> $filename
        size=$(stat -c %s $filename)
done
 
# Chop off any excess
head -c $max_size $filename > $tmpfilename
mv $tmpfilename $filename
