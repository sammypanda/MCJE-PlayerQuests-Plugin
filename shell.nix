with (import <nixpkgs> {});

mkShell {
  buildInputs = [
    maven
    jdk23
    glibc
  ];

  shellHook = ''
    export JAVA_HOME=${jdk23.outPath}
  '';
}
