package com.yourpackage; // Replace with your actual package name

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import android.content.pm.PackageManager; 
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
	
	private static final int PICK_EXCEL_FILE = 1;
	private TextView resultTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		resultTextView = findViewById(R.id.resultTextView);
		Button selectFileButton = findViewById(R.id.selectFileButton);
		
		// Request permission to read external storage
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
		}
		
		selectFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectFile();
			}
		});
	}
	
	private void selectFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		startActivityForResult(intent, PICK_EXCEL_FILE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
			readExcelFile(uri);
		}
	}
	
	private void readExcelFile(Uri uri) {
		StringBuilder result = new StringBuilder();
		try (InputStream inputStream = getContentResolver().openInputStream(uri);
		Workbook workbook = new XSSFWorkbook(inputStream)) {
			
			Sheet sheet = workbook.getSheetAt(0);
			for (Row row : sheet) {
				for (Cell cell : row) {
					RichTextString richText = cell.getRichStringCellValue();
					int length = richText.length();
					for (int i = 0; i < length; i++) {
						String charValue = String.valueOf(richText.getString().charAt(i));
						Font font = workbook.getFontAt(richText.getFontAtIndex(i));
						String fontName = font.getFontName();
						result.append("Character: ").append(charValue).append(", Font: ").append(fontName).append("\n");
					}
				}
			}
			} catch (Exception e) {
			Log.e("ExcelReader", "Error reading Excel file", e);
			result.append("Error reading file: ").append(e.getMessage());
		}
		displayResults(result.toString());
	}
	
	private void displayResults(String result) {
		resultTextView.setText(result);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission granted, you can proceed with file selection
				} else {
				// Permission denied, show a message to the user
				resultTextView.setText("Permission denied to read external storage.");
			}
		}
	}
}
