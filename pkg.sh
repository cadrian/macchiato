#!/bin/bash

cd Macchiato
mvn package || exit 1

cd ..

rm -rf Macchiato.deb
cp -a Macchiato Macchiato.deb

cd Macchiato.deb
mv pom.xml pom-orig.xml
mv pom-debian.xml pom.xml
exec debuild -us -uc -b
