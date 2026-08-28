# TrailCharter launcher status

FINAL landscape identity: the approved mountains / trees / river / sun composition. The landscape artwork itself must not be redesigned.

Launcher container status: EXPLORE / active physical-device test. Physical-device testing showed that the no-shield full-bleed landscape is a poor fit for Android adaptive masks: 0.1.1 appeared oversized/cropped, 0.1.2 exposed duplicate adaptive artwork, and 0.1.3 removed the duplicate but still produced an awkward framed result on device. The no-shield launcher treatment is therefore not accepted.

Version `0.1.4-foundation` tests the previously approved shield treatment specifically for the Android launcher. The shield contains the same FINAL landscape composition and is centred on the brand dark-green field (`#1F3D2E`), allowing Android to crop the surrounding field for circle, square and rounded-square masks instead of cropping the landscape itself. The adaptive foreground contains one shield composition only; the adaptive background is plain dark green.

PR #5 (`Test approved shield launcher treatment`) passed PR Android CI run #19, including unit tests, lint, debug assembly and APK upload, and was merged into `main` as `3b8dc8410839b459d5f586861690c7af3c6beada`. Post-merge Android CI run #20 also passed and produced `trailcharter-debug-run-20` (artifact ID `9691845325`).

The verified APK is versionCode 5 / versionName `0.1.4-foundation`; its SHA-256 is `7e596b7dc9edbcac13ed342214134b01319ced79c20c160d3ff892f0b0ec48a8`. Physical-device confirmation is required before the shield treatment is accepted or made FINAL.
