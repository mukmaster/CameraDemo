# CameraDemo-App
Zeigt, wie man relativ einfach auf die/eine vorhandene Kamera-App zugreifen kann. 

## Wie funktioniert die App?
Diese App - und ihre einzige Klasse MainActivity - demonstrieren, wie mit Hilfe der im Smartphone 
vorhandenen Kamera-App ein Foto aufgenommen und an die App weitergegeben werden kann. Sie besitzt Buttons, 
die mit AUFNAHME1 und AUFNAHME2 beschriftet sind. Diese stehen für zwei unterschiedliche Ansätze:

* AUFNAHME1: Die Thumbnail-Bitmap wird direkt aus den von der Kamera-App zurückgelieferten "Extra-Daten" ausgelesen.
* AUFNAHME2: Das Bild wird auf dem Externspeicher gespeichert und dann von dieser App über seine imageUri ausgelesen.

In beiden wird ein Intent mit der Aktion MediaStore.ACTION_IMAGE_CAPTURE erzeugt, der zur Kamera-App des Smartphones wechselt. 
 
**Achtung:** Nur die zweite Methode funktioniert auf allen aktuellen Smartphones, sie hat allerdings den Nachteil, 
dass ein Zugriff auf den externen Speicher (und Schreibrechte dafür) notwendig sind. 
Die erste Methode hingegen setzt voraus, dass das SmartPhone die neue Camera2 API verwendet. Funktioniert mit Nexus oder Pixel-Geräten mit neueren Android-Versionen,
versagt aber auf Smartphones vieler anderer Hersteller.

## Bekannte Probleme
* AUFNAHME1 funktioniert möglicherweise nur auf Google-Smartphones (Pixel oder Nexus). Geräte anderer
Hersteller sind zum Teil unvollständig implementiert und liefern im Intent-Extra keine Bitmap, sondern NULL.  
* AUFNAHME2 benötigt Schreibrechte auf dem externen Speicher des Smartphones

## Credits
* Der Source Code für den ersten Ansatz basiert auf der Android-Dokumentation für Entwickler: 
"Take photos with a camera app", siehe https://developer.android.com/training/camera/photobasics#TaskCaptureIntent
* Der zweite Ansatz stammt von JorgeSys, siehe https://github.com/Jorgesys/Android-CameraTakePicture 
* Bildquelle: Photographer - Through the Camera Lens. By Lanty. Attribution 2.0 Generic (CC BY 2.0)