package com.roderus.app.camerademo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Diese App - und ihre einzige Klasse MainActivity - demonstrieren, wie mit Hilfe der im Smartphone
 * vorhandenen Kamera-App ein Foto aufgenommen und an die App weitergegeben werden kann.
 *
 * <p><a href="https://github.com/mukmaster/CameraDemo" target="_blank">Details bitte nachlesen im README.md auf GitHub</a></p>
 * @author Helmut Roderus
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CameraDemo";  // TAG zur Identifikation der App im Log
    private static final int REQUEST_TYP1 = 111;   // Der "Intent-Code" für Methode 1
    private static final int REQUEST_TYP2 = 112;   // Der "Intent-Code" für Methode 2
    private ImageView mImageView;                    // ImageView für die Anzeige des Bilds
    private Uri imageUri;                            // Adresse (URI) des extern gespeicherten Bilds

    /**
     * Initialisierung der MainActivity. Lädt das Defaultbild aus Drawables in die Anzeige.
     * @param savedInstanceState    Falls Daten von Instanz zu Instanz der Activity weitergegeben werden soll
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.photoFrame);
        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.camera_m));
    }

    /**
     * onClick ist die Event-Handling-Methode für die beiden Buttons AUFNAHME1 und AUFNAHME2.
     * Sie bereitet den Intent vor, der die Kamera-App startet, und schickt ihn ab.
     *
     * @param v   Das View-Objekt, das gerade angeklickt wurde (also button1 oder button2)
     */
    public void onClick(View v)
    {
        // Erzeuge einen Standard-Intent, um damit die Camera-App zu aktivieren
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Erst noch prüfen, ob der Intent überhaupt implementiert ist
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Nun die Frage: Welcher Button wurde eigentlich angeklickt?
            // Der Intent muss entsprechend der Methode unterschiedlich vorbereitet werden
            switch (v.getId()) {
                // War es der erste Button (AUFNAHME1)? Dann ist es recht einfach
                case R.id.button1:
                    // Wir starten die Kamera-Activity mit REQUEST_TYP1 und warten in onActivityOnResult auf das Ergebnis
                    startActivityForResult(takePhotoIntent, REQUEST_TYP1);
                    break;
                // Oder war es der zweite Button (AUFNAHME2)? Es wird (ein wenig) komplizierter
                case R.id.button2:
                    // Kamera-Activity mit REQUEST_TYP2 starten und in onActivityOnResult auf das Ergebnis warten
                    // In Android 6.0 und neuer muss die App erst fragen, ob das Bild auf den externen Speicher geschrieben werden darf
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(!checkExternalStoragePermission()) return; // wenn nicht, dann machen wir gar nix
                    }
                    // Jetzt erzeugen wir ein ContentValues-Objekt für den Intent und füllen es gleich mit Bild-Metadaten
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "Mein Foto");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Aufnahmedatum: " + System.currentTimeMillis());
                    // Eine Zeile mit "values" einfügen und die URI zurückbekommen
                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    // Dem Intent die URI als Extra mitgeben
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    // und schließlich den Intent losschicken
                    startActivityForResult(takePhotoIntent, REQUEST_TYP2);
                    break;
                default:
                    // Es wurde kein bekannter Button angeklickt. Nichts zu tun.
                    return;
            }
        }
    }

    /**
     * onActivityResult ist die Event-Handling-Methode, die bei der Rückkehr aus der Kamera-App
     * aufgerufen wird. Hier werden die Extra-Daten (das Foto bzw. ein Verweis darauf) aus dem
     * Intent gelesen und ausgewertet.
     *
     * @param requestCode   Der Code, den der Intent anfänglich mitbekommen kann
     * @param resultCode    Der Resultcode, den die Kamera-App zurückliefert
     * @param intent        Das Intent-Objekt, mit dem die Kamera-App hierher zurückkam
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            // Die Kamera-App liefert einen Fehlercode, wir brauchen nicht weiter machen
            return;
        }
        Bitmap imageBitmap;     // zum Speichern des Verweises auf das Bild
        switch (requestCode) {
            case REQUEST_TYP1:
                // AUFNAHME1:  Die CameraApp sollte uns eine Bitmap (das Foto) als Extra mitgebracht haben
                Bundle extras = intent.getExtras();
                // Extras (Bitmap) auspacken und anzeigen
                imageBitmap = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);
                break;
            case REQUEST_TYP2:
                // AUFNAHME2: Die CameraApp hat das Bild extern gesepeichert und wir können über die URI zugreifen
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    mImageView.setImageBitmap(imageBitmap);
                    showPathFromURI(imageUri);
                } catch (Exception e) {
                    // Das Lesen des Bildes kann schief gehen, dann landen wir hier
                    e.printStackTrace();
                }
                break;
            default:
                // Kein bekannter Request-Code: Nichts weiter zu tun
                return;
        }
    }

    /**
     * Hilfsmethode, die den Pfad zum Bild auf dem externen Speiocher ermittelt und als Toast kurz anzeigt
     *
     * @param contentUri  Die URI, unter der das Bild abgelegt ist
     */
    private void showPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        Toast.makeText(MainActivity.this,"Pfad des Bildes: " + cursor.getString(column_index), Toast.LENGTH_SHORT).show();
    }

    /**
     * Hilfsmethode, die prüft, ob die App die Erlaubnis zum Schreiben auf den externen Speicher besitzt
     * @return true   falls die Schreiberlaubnis vorliegt
     */
    private boolean checkExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Erlaubnis anfordern
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
        } else {
            // Erlaubnis ist bereits erteilt
            return true;
        }
        return false;
    }

}
