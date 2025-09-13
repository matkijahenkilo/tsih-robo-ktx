{
  description = "Cute Discord bot named Tsih-robo-ktx";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    # starting from Maven 3.9.10, it stopped working correctly on Aarch64 systems.
    # So I am using 3.9.9 for this flake.
    nixpkgs-working-maven.url = "github:nixos/nixpkgs/8c5a555e338b5a30704c45de55e016eabf9c55c7";
    systems.url = "github:nix-systems/default";
    flake-parts.url = "github:hercules-ci/flake-parts";
    treefmt-nix = {
      url = "github:numtide/treefmt-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs =
    inputs:
    inputs.flake-parts.lib.mkFlake { inherit inputs; } {
      systems = import inputs.systems;
      imports = [
        inputs.treefmt-nix.flakeModule
        inputs.flake-parts.flakeModules.easyOverlay
      ];

      perSystem =
        {
          config,
          lib,
          pkgs,
          ...
        }:
        {
          packages = rec {
            default = tsih-robo-ktx;
            tsih-robo-ktx = pkgs.callPackage ./default.nix {
              maven = inputs.nixpkgs-working-maven.legacyPackages.${pkgs.system}.maven;
            };
          };

          apps.default = {
            type = "app";
            program = "${config.packages.default}/bin/tsih-robo-ktx";
          };

          overlayAttrs = {
            inherit (config.packages) tsih-robo-ktx;
          };

          devShells.default = import ./shell.nix {
            inherit pkgs;
            pkgMvn = inputs.nixpkgs-working-maven.legacyPackages.${pkgs.system};
            inputsFrom = lib.attrsets.attrValues config.packages;
          };

          treefmt.config = {
            projectRootFile = "flake.nix";
            programs = {
              nixfmt.enable = true;
              prettier.enable = true;
            };
          };
        };
    };
}
