LOOM_BIN := /Users/yyc/graal/loom/build/macosx-x86_64-server-release/images/graal-builder-jdk/

cont: export PATH = $(LOOM_BIN)/bin
cont: src/cont/Main.java
	cd src && (javac cont/Main.java && java cont.Main)

vthread: export PATH = $(LOOM_BIN)/bin
vthread: src/vthread/Main.java
	cd src && (javac vthread/Main.java && java vthread.Main)