package developer.rajatjain.pdf2txt;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

import com.tom_roush.pdfbox.cos.COSDocument;
import com.tom_roush.pdfbox.pdfparser.PDFParser;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MyIntentService extends IntentService {

    ResultReceiver rec;
    private static final String EXTRA_PARAM1 = "developer.rajatjain.pdf2txt.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "developer.rajatjain.pdf2txt.extra.PARAM2";

    public MyIntentService() {
        super("MyIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            File file = (File) intent.getSerializableExtra(EXTRA_PARAM1);

            rec = intent.getParcelableExtra(EXTRA_PARAM2);

            RenderDo(file,file.getName());
        }
    }

    public static void startActionFoo(Context context, File file, MyResultReceiver mReceiver) {
        Intent intent = new Intent(context, MyIntentService.class);

        intent.putExtra(EXTRA_PARAM1, file);
        intent.putExtra(EXTRA_PARAM2, mReceiver);
        context.startService(intent);
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
            sendResult(RESULT_CANCELED);
        }
    }

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            String PATH_open = gpxfile.getAbsolutePath();
            Log.e("MainActivity", "Path saved at " + PATH_open);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            sendResult(RESULT_OK);

        } catch (IOException e) {
            e.printStackTrace();
            sendResult(RESULT_CANCELED);
        }
    }

    private void sendResult(int result) {
        Bundle b = new Bundle();
        rec.send(result, b);
    }
}


