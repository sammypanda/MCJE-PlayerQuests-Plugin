with (import <nixpkgs> {});

mkShell {
  buildInputs = [
    maven
    jdk22
    glibc
  ];
}
