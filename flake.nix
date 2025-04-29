{
  description = "Private Discord bot named Tsih-robo-ktx";

  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";

  outputs = { nixpkgs, ... }:
    let
      pkgs = import nixpkgs {
        system = "x86_64-linux";
      };
    in {
      packages.x86_64-linux = rec {
        default = tsih-robo-ktx;
        tsih-robo-ktx = pkgs.callPackage ./default.nix { };
      };
    };
}