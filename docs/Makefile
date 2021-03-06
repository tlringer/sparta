# This is a nearly unmodified version of the
# Checker Framework manual Makefile.

# Put user-specific changes in your own Makefile.user.
# Make will silently continue if that file does not exist.
-include Makefile.user

PLUME_LIB ?= ../../plume-lib
CHECKERFRAMEWORK ?= ../../checker-framework/checker

# To regenerate this list, run:   latex-process-inputs -makefilelist manual.tex
TEX_FILES = \
manual.tex \
manual-style.tex \
macros.tex \
introduction.tex \
installation.tex \
flow.tex \
flow-flow-policy.tex \
permission-table.tex \
intents.tex \
annotated-app.tex \
appanalysis.tex \
app-annotation.tex \
rules-of-engagement.tex \
sparta-internals.tex \
tutorial.tex

all: figures-all manual.pdf manual.html # javadoc

.PHONY: figures-all
figures-all:
	${MAKE} -C figures all

# TODO: once Javadoc is enabled
#javadoc:
#	ln -sf ../javadoc doc

manual.dvi: ${TEX_FILES} bib-update
	$(MAKE) -C figures
	latex manual.tex
	bibtex manual
	latex manual.tex
	while grep "Rerun to get" manual.log; do latex manual.tex; done

manual.pdf: manual.dvi bib-update
	pdflatex manual.tex
	bibtex manual
	pdflatex manual.tex
	while grep "Rerun to get" manual.log; do pdflatex manual.tex; done

manual.ps: manual.dvi
	dvips -P cmz -t letter -o manual.ps manual.dvi

html: manual.html
manual.html: manual.dvi
	hevea -fix -exec xxdate.exe urlhref.hva manual.tex
# This was necessary once, but it makes Hevea 2.06 use wrong section anchors.
#	hevea -exec xxdate.exe urlhref.hva manual.tex
# The following three lines are only necessary when using Hevea before version 2.04
# With version 2.04 they have no effect but do no harm.
	\mv -f manual.html manual.html-with-htoc
	./hevea-retarget-crossrefs < manual.html-with-htoc > manual.html
	\rm -f manual.html-with-htoc
	perl -pi -e 's/\/&#XA0;/\/~/' manual.html
# The below is not necessary since Hevea mkes good tables of contents itself.
#	perl -pi -e 's/(<!--TOC section Introduction-->)/$$1\n\n<p>Contents:<\/p>\n<!-- start toc.  do not edit; run html-update-toc instead -->\n<!-- end toc -->\n\n<br>\n/' manual.html
#	html-update-toc manual.html

export BIBINPUTS = $BIBINPUTS:.:bib

bib:
ifdef PLUMEBIB
	ln -s ${PLUMEBIB} bib
else
	git clone https://github.com/mernst/plume-bib.git bib
endif
.PHONY: bib-update
bib-update: bib
# Even if this command fails, it does not terminate the make job.
# However, to skip it, invoke make as:  make NOGIT=1 ...
ifndef NOGIT
	-(cd bib && git pull && make)
endif

# Update files that were copied from elsewhere to make the manual
# self-contained and able to be built without first installing (which
# requires one to read the manual...)
copies-up-to-date:
ifeq "$(wildcard ${CHECKERFRAMEWORK}/manual/manual-style.tex)" "${CHECKERFRAMEWORK}/manual/manual-style.tex"
# file exists
	cp -pf ${CHECKERFRAMEWORK}/manual/manual-style.tex .
endif
ifeq "$(wildcard ${PLUME_LIB}/bin/hevea-retarget-crossrefs)" "${PLUME_LIB}/bin/hevea-retarget-crossrefs"
# file exists
	cp -pf ${PLUME_LIB}/bin/hevea-retarget-crossrefs .
endif

clean:
	@\rm -f doc *.aux *.blg *.haux *.htoc *.log *.pdf *.ps *.dvi *.idx *.ind *.ilg *.toc

very_clean: clean
	@\rm -f manual.html
	${MAKE} -C figures clean

.PHONY: checklink
checklink:
	${INV}/scripts/checklink -q -e `grep -v '^#'` ${INV}/scripts/checklink-args.txt` manual.html

.PHONY: tags
TAGS: tags
tags: ${TEX_FILES}
	etags ${TEX_FILES}
