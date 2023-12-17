with (import <nixpkgs> {});

mkShell {
  buildInputs = [
    maven
    jdk17
    glibc
  ];
}