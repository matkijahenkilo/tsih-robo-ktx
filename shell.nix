{
  pkgs ? import <nixpkgs> { },
  ...
}:
pkgs.mkShell {
  shellHook = ''
    echo -e "\u001B[34mNanora!"
  '';

  nativeBuildInputs = with pkgs; [
    maven
  ];
}