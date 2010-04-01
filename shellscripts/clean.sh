#!/bin/bash

# Copyright (c) 2010 Stanford University
#
# Permission to use, copy, modify, and distribute this software for any
# purpose with or without fee is hereby granted, provided that the above
# copyright notice and this permission notice appear in all copies.
#
# THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
# WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
# ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
# WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
# ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
# OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
#

# Checks a file for common style errors.
# Without any arguments, this script simply checks on the occurance of these
# errors and reports on them. If passed in the string "fix", it attempts to
# fix the errors when possible (though not all errors can be fixed).

# If you really want to get around these restrictions, add the appropriate magic
# values somewhere in your file:
# "Ignore Trailing Whitespace", "Ignore Tabs", "Ignore Windows Newlines"

# Possible additions: 80 character lines, spacing for ifs, function definitions,
# etc., checking for copyright notices/dates.


fix=0
if [ "$1" == "fix" ]; then
	fix=1
fi

fileName="$2"

containsErrors=0

function check {
	fileName=$1
	file=$2
	magic=$3
	regexp=$4
	sed=$5
	error=$6

	echo "$file" | grep -q "$magic" # Check if we should ignore the issue
	if [ $? -eq 1 ]; then
		if [ -n "$sed" ] && [ "$fix" -eq 1 ]; then
			sed -i "$sed" "$fileName" # Fix the issue
		else 
			while read line; do
				containsErrors=1 # Either we can't or don't want to fix issues
				lineNum=$( echo $line | grep -Eo "[0-9]+" | head -n 1 | tr -d "\n" )
				echo "${fileName}:$lineNum: error: $error"
			done < <(echo "$file" | grep -En "$regexp")
		fi
	fi
}

#if [[ ! -t 0 ]]; then
#	echo "hi"
#	file=`cat`
#else
#	echo "bye"
#	file=`cat $fileName`
#fi

file=""
if [[ -t 0 ]]; then
	file=`cat $fileName`
else
	file=`cat`
	if [ -z "$file" ]; then
		file=`cat $fileName`
	fi
fi

#ADD NEW RULES HERE
#check $file "" "" "" ""
check "$fileName" "$file" "Ignore Tabs" "	" "" "Tab characters not allowed"
check "$fileName" "$file" "Ignore Trailing Whitespace" "[ 	]+$" "s/[ \t]*$//g" "Trailing whitespace not allowed"
check "$fileName" "$file" "Ignore Windows Newlines" "" "s///g" "Windows newlines not allowed"

exit $containsErrors
