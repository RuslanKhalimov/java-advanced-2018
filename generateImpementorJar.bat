SET lib=.\lib\*
SET test=.\artifacts\JarImplementorTest.jar
SET manifest=..\MANIFEST.MF
SET dependencies=info\kgeorgiy\java\advanced\implementor\

javac -d out -cp %lib%;%test% java\ru\ifmo\rain\khalimov\implementor\Implementor.java

cd out
jar xf ..\%test% %dependencies%Impler.class %dependencies%JarImpler.class %dependencies%ImplerException.class
jar cfm Implementor.jar %manifest% ru\ifmo\rain\khalimov\implementor\*.class %dependencies%*.class