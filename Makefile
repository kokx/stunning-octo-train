JUNIT_HOME=Common/lib
JC_HOME=Common/lib/java_card_kit-2_2_1

JC_PATH=${JC_HOME}/lib/apdutool.jar:${JC_HOME}/lib/apduio.jar:${JC_HOME}/lib/converter.jar:${JC_HOME}/lib/jcwde.jar:${JC_HOME}/lib/scriptgen.jar:${JC_HOME}/lib/offcardverifier.jar:${JC_HOME}/lib/api.jar:${JC_HOME}/lib/installer.jar:${JC_HOME}/lib/capdump.jar:${JC_HOME}/samples/classes:${CLASSPATH}

GP=java -jar Terminal/lib/gp.jar
CONVERTER=java -Djc.home=${JC_HOME} -classpath ${JC_PATH}:CalcApplet/bin com.sun.javacard.converter.Converter

CLASSFILES_TERM := $(shell find Terminal/src/* -type f -regex .*java | sed 's/src/bin/g' | sed 's/java/class/g')
CLASSFILES_APPL := $(shell find CalcApplet/src/* -type f -regex .*java | sed 's/src/bin/g' | sed 's/java/class/g')
APPLETFILES := $(shell find CalcApplet/src/* -type f -regex .*java | sed 's/src/jc-bin/g' | sed 's/java/class/g')

# Fill this with your own AIDs
PID=0x3B:0x29:0x63:0x61:0x6C:0x63
CalcAppletAID=${PID}:0x01
OpelAppletAID=${PID}:0x02

PIDplain=$(shell echo ${PID} | sed 's/://g' | sed 's/0x//g')
CalcAppletAIDplain=$(shell echo ${CalcAppletAID} | sed 's/://g' | sed 's/0x//g')
OpelAppletAIDplain=$(shell echo ${OpelAppletAID} | sed 's/://g' | sed 's/0x//g')

applet: ${APPLETFILES} CalcApplet/jc-bin/applet/javacard/applet.cap

.PHONY: terminal,run-terminal,clean,clean-terminal,applet

terminal: ${CLASSFILES_TERM}

run-calc-terminal:
	java -cp Terminal/bin terminal.CalcTerminal

calc-terminal: terminal run-calc-terminal

run-opel-terminal:
	java -cp Terminal/bin terminal.OpelTerminal

opel-terminal: terminal run-opel-terminal

clean:
	rm CalcApplet/jc-bin/applet/javacard/applet.cap
	${GP} --delete ${PIDplain}

clean-terminal:
	rm ${CLASSFILES_TERM}

CalcApplet/jc-bin/applet/javacard/applet.cap: ${APPLETFILES}
	# Convert to .cap...
	@${CONVERTER} -v -out CAP -exportpath ${JC_HOME}/api_export_files -classdir CalcApplet/jc-bin -d CalcApplet/jc-bin \
		-applet ${CalcAppletAID} applet.CalcApplet \
		-applet ${OpelAppletAID} applet.OpelApplet \
		applet ${PID} 1.0
	# Installing applet...
	@${GP} --install CalcApplet/jc-bin/applet/javacard/applet.cap --default
	# Creating applet instances...
	@echo Card applet AID: ${CalcAppletAIDplain}
	@${GP} --applet ${CalcAppletAIDplain} --package ${PIDplain} --create ${CalcAppletAIDplain}31
	@${GP} --applet ${OpelAppletAIDplain} --package ${PIDplain} --create ${OpelAppletAIDplain}31
	# DONE

Terminal/bin/%.class: Terminal/src/%.java
	javac -d Terminal/bin $^

CalcApplet/jc-bin/%.class: CalcApplet/src/%.java
	@mkdir -p CalcApplet/jc-bin
	# compiling $^
	@javac -source 1.3 -target 1.1 -d CalcApplet/jc-bin -cp Terminal/lib/jcardsim-2.2.1-all.jar:\
	Terminal/lib/gp.jar:\
	Terminal/src:\
	CalcApplet/src $^
