#!/bin/bash

if [ $# -lt 1 ]; then
  echo "Usage: $0 old_tag [new_tag|HEAD]"
  echo "  e.g.: $0 v4.1.6 v4.1.7"
  exit -1
fi

OLD_COMMIT=$1
NEW_COMMIT=${2:-HEAD}
TARGET=target/release_notes

mkdir -p ${TARGET}
git diff ${OLD_COMMIT} ${NEW_COMMIT} --minimal --color | ansi2html > ${TARGET}/release_diff.html
git diff ${OLD_COMMIT} ${NEW_COMMIT} --stat=200,200 | ansi2html > ${TARGET}/release_changes.html
git log ${OLD_COMMIT}..${NEW_COMMIT} --format='%an' | sort | uniq | ansi2html > ${TARGET}/release_authors.html
git log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%ci) %C(bold blue)<%an>%Creset' --abbrev-commit ${OLD_COMMIT}..${NEW_COMMIT} -- | ansi2html > ${TARGET}/release_log.html
