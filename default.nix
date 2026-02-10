{
  lib,
  maven,
  jdk25, # jre25_minimal can't run the project
  makeWrapper,

  ffmpeg,
  yt-dlp,
  gallery-dl,
}:
let
  project-name = "tsih-robo-ktx";
  vers = "1.0.0";
  jar-file = "${project-name}-${vers}-jar-with-dependencies.jar";
in
maven.buildMavenPackage {
  pname = project-name;
  version = vers;

  src = lib.cleanSource ./.;

  # Maven uses the internet for downloading deps, thus you need to manually
  # update the mvnHash when they change. You can set it to `lib.fakeHash` and
  # get the right one from the error message
  mvnHash = "sha256-LQ4DYRlGN4Aq/zdS+eOCEsyT/14t87aEpTwfK9zaD/I=";

  nativeBuildInputs = [ makeWrapper ];

  installPhase = ''
    runHook preInstall

    mkdir -p $out/bin $out/share/${project-name}
    install -Dm644 target/${jar-file} $out/share/${project-name}

    makeWrapper ${jdk25}/bin/java $out/bin/${project-name} \
      --add-flags "-Xms128m -Xmx512m -XX:+UseSerialGC -XX:+UseCompactObjectHeaders -XX:+ExitOnOutOfMemoryError -Xss256k --enable-native-access=ALL-UNNAMED -jar $out/share/${project-name}/${jar-file}" \
      --prefix PATH : ${
        lib.makeBinPath [
          ffmpeg
          yt-dlp
          gallery-dl
        ]
      }

    runHook postInstall
  '';

  meta = {
    mainProgram = "tsih-robo-ktx";
    homepage = "https://github.com/matkijahenkilo/tsih-robo-ktx";
    platforms = lib.platforms.x86_64 ++ lib.platforms.aarch64;
    maintainers = with lib.maintainers; [ matkijahenkilo ];
  };
}
