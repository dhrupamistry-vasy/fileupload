package com.example.demo.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.message.ResponseMessage;
import com.example.demo.service.FilesStorageService;


@Controller
public class FilesController {

	@Autowired
	FilesStorageService storageService;

	private final String UPLOAD_DIR = "C:\\Users\\dhrupamistry\\Downloads"; // Update this with your directory path

	@PostMapping("/upload")
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		List<String> uploadedFileNames = new ArrayList<>();
		try {
			// Create the directory if it doesn't exist
			File uploadDir = new File(UPLOAD_DIR);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}

			// Save the file to the specified directory
			String filePath = UPLOAD_DIR + File.separator + file.getOriginalFilename();
			file.transferTo(new File(filePath));

			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (IOException e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}
	  @PostMapping("/append/{fileName}")
	    public ResponseEntity<ResponseMessage> appendToFile(@PathVariable String fileName, @RequestBody String text) {
	        String message = "";
	        try {
	            // Construct the file path
	            String filePath = UPLOAD_DIR + File.separator + fileName;
	            File file = new File(filePath);
	            
	            // Check if the file exists
	            if (!file.exists()) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage("File not found"));
	            }

	            // Append the text to the file
	            FileWriter writer = new FileWriter(file, true);
	            writer.append(text);
	            writer.close();

	            message = "Appended text to file successfully.";
	            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
	        } catch (IOException e) {
	            message = "Could not append text to file: " + e.getMessage();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
	        }
	    }



	@GetMapping("/download/{fileName}")
	public ResponseEntity<Object> downloadFile(@PathVariable String fileName) {
		ResponseEntity<Object> response = null;
		try {

			Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
			File file = filePath.toFile();

			if (file.exists()) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentDispositionFormData("attachment", fileName);

				response = ResponseEntity.ok().headers(headers).contentLength(file.length())
						.body(new FileSystemResource(file));
			} else {
				response = ResponseEntity.notFound().build();
			}
		} catch (Exception ex) {
			response = ResponseEntity.status(500).body("Failed to download the file: " + ex.getMessage());
		}

		return response;
	}

}
