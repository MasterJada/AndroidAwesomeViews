#!/usr/bin/env bash

mkdir report/test-results
modules=("app" "common")
for module in "${modules[@]}"
do

    testsDir=""
    testsDir="$module/build/test-results/testDebugUnitTest"


    if [ ! "$(ls -A $testsDir)" ]; then
        echo "Unit tests report wasn't found for module: $module"
        continue
    fi

    # copy all files inside, to our folder
    cp $testsDir/* report/test-results/

done