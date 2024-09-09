with (import <nixpkgs> {});

mkShell {
  buildInputs = [
    maven
    jdk22
    glibc
  ];

  shellHook = ''
    export JAVA_HOME=${jdk22.outPath}
  '';
}
