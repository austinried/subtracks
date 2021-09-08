#!/bin/bash
set -e

dir=$(dirname "$(readlink -f "$0")")
root=$(realpath "$dir/..")
assets="$root/android/app/src/main/assets"

for filename in "$assets"/licenses/*.txt; do
    [ -e "$filename" ] || continue
    moreLicenses="$moreLicenses"'<hr/><pre style="white-space: pre-wrap;">
'$(cat "$filename")'
</pre>
'
done
echo "$moreLicenses" > "$assets/moreLicenses"

awk '
/<\/body>/ {
    system ( "cat \"'"$assets/moreLicenses"'\"" )
}
{ print }
' "$assets/licenses.html" > "$assets/output.html"

mv "$assets/output.html" "$assets/licenses.html"
rm "$assets/moreLicenses"
