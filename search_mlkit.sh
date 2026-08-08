#!/bin/bash
curl -s "https://dl.google.com/dl/android/maven2/com/google/android/gms/group-index.xml" | grep -o -E "play-services-mlkit-[a-z-]+" | sort | uniq
