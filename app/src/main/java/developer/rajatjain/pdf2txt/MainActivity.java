package developer.rajatjain.pdf2txt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.text.TextPosition;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MyResultReceiver.Receiver {
    File root;
    AssetManager assetManager;
    Bitmap pageImage;
    TextView tv;
    Button selectPdf, RenderPdf;
    String PATH_FILE = "";
    String PATH_open = "";
    ProgressDialog progressDialog;
    File FILE;
    private MyResultReceiver mReceiver;
    private static final int PERMISSION_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Rendering...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        mReceiver = new MyResultReceiver(new Handler());

        mReceiver.setReceiver(this);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE
                );

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        setup();
        tv = (TextView) findViewById(R.id.statusTextView);
        selectPdf = (Button) findViewById(R.id.bselect);
        selectPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                Intent i = Intent.createChooser(intent, "File");
                startActivityForResult(i, 1);
                tv.setText("processing");
                //setProgressBarIndeterminateVisibility(true);


            }
        });
        RenderPdf = (Button) findViewById(R.id.buttonRender);
        RenderPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                MyIntentService.startActionFoo(MainActivity.this, FILE, mReceiver);
//                RenderDo(FILE, FILE.getName());
              //  progressDialog.hide();

            }
        });
       /* try {
            PDFParserTextStripper pdfParserTextStripper=new PDFParserTextStripper();
            PDDocument document1 = PDDocument.load(assetManager.open("Test.pdf"));
            pdfParserTextStripper.stripPage(0,document1);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    /*    PDDocument pdd = null;
        try {
            pdd = PDDocument.load(assetManager.open("Test.pdf"));
            PDFParserTextStripper stripper = new PDFParserTextStripper();
            stripper.setSortByPosition(true);
            for (int i=0;i<pdd.getNumberOfPages();i++){
                stripper.stripPage(i,pdd);

            }
            Log.e("MainActivity","Test 2");
            PDFTextStripper pdfStripper=new PDFTextStripper();
            PDDocument pdDoc = PDDocument.load(assetManager.open("Test.pdf"));
            pdDoc.getNumberOfPages();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);
            Log.e("MainActivity",pdfStripper.getText(pdDoc));
        } catch (IOException e) {
            // throw error
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

    /**
     * Initializes variables used for convenience
     */
    private void setup() {
        // Enable Android-style asset loading (highly recommended)
        PDFBoxResourceLoader.init(getApplicationContext());
        // Find the root of the external storage.
        root = android.os.Environment.getExternalStorageDirectory();
        assetManager = getAssets();
        tv = (TextView) findViewById(R.id.statusTextView);
    }


    /**
     * Loads an existing PDF and renders it to a Bitmap
     */
    public void renderFile(View v) {
        // Render the page and save it to an image file
        try {
            // Load in an already created PDF
            PDDocument document = PDDocument.load(assetManager.open("Created.pdf"));
            // Create a renderer for the document
            PDFRenderer renderer = new PDFRenderer(document);
            // Render the image to an RGB Bitmap
            pageImage = renderer.renderImage(0, 1, Bitmap.Config.RGB_565);

            // Save the render result to an image
            String path = root.getAbsolutePath() + "/Download/render.jpg";
            File renderFile = new File(path);
            FileOutputStream fileOut = new FileOutputStream(renderFile);
            pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
            fileOut.close();
            tv.setText("Successfully rendered image to " + path);
            // Optional: display the render result on screen
            displayRenderedImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Strips the text from a PDF and displays the text on screen
     */
    public void stripText(View v) {
        String parsedText = null;
        PDDocument document = null;
        try {
            document = PDDocument.load(assetManager.open("Hello.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(1);
            parsedText = "Parsed text: " + pdfStripper.getText(document);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (document != null) document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tv.setText(parsedText);
    }

    /**
     * Helper method for drawing the result of renderFile() on screen
     */
    private void displayRenderedImage() {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = (ImageView) findViewById(R.id.renderedImageView);
                        imageView.setImageBitmap(pageImage);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        progressDialog.hide();
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            tv.setText("Saved");
        }
    }

    class PDFParserTextStripper extends PDFTextStripper {

        public PDFParserTextStripper() throws IOException {
            super();
        }


        public void stripPage(int pageNr, PDDocument document1) throws IOException {
            this.setStartPage(pageNr + 1);
            this.setEndPage(pageNr + 1);
            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            writeText(document1, dummy); // This call starts the parsing process and calls writeString repeatedly.
        }


        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            for (TextPosition text : textPositions) {
                Log.e("MainActivity", "String[" + text.getXDirAdj() + "," + text.getYDirAdj() + " fs=" + text.getFontSizeInPt() + " xscale=" + text.getXScale() + " height=" + text.getHeightDir() + " space=" + text.getWidthOfSpace() + " width=" + text.getWidthDirAdj() + " ] " + text.getUnicode());
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    PATH_FILE = uri.getPath();
                    if (!PATH_FILE.equals("")) {
                        File file = new File(PATH_FILE);
                        Log.e("MainActivity", "PATH=" + PATH_FILE);
                        String name = file.getName();
                        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                        FILE = file;
                        RenderPdf.setVisibility(View.VISIBLE);
                    }
                  /*  if (PATH_FILE.substring(0, 1).equals("c")) { // ES
                        PATH_FILE = "file://" + PATH_FILE.substring(28,PATH_FILE.length());
                    }
                    if (PATH_FILE.toLowerCase().startsWith("file://")) {
                        PATH_FILE = (new File(URI.create(PATH_FILE))).getAbsolutePath();
                    }*/
                }
            }

        }
    }

  /*  public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            PATH_open = gpxfile.getAbsolutePath();
            Log.e("MainActivity", "Path saved at " + PATH_open);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
            tv.setText("Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RenderDo(File file, String name) {
        try {
            // if (extension.toLowerCase().equals("pdf")) {

            PDFParser parser = null;
            parser = new PDFParser(new FileInputStream(file));
            parser.parse();
            COSDocument cosDoc = null;
            cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = new PDDocument(cosDoc);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(pdDoc.getNumberOfPages());
            String parsedText = pdfStripper.getText(pdDoc);
            Log.e("MainActivity", "selected pdf:" + name + "\n content\n" + pdfStripper.getText(pdDoc));
            generateNoteOnSD(this, name + "TXT", pdfStripper.getText(pdDoc));
            //setProgressBarIndeterminateVisibility(false);


            //} else {
            //    Log.e("MainActivity", "Not a pdf it is "+extension);
            //    return;
            // }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
