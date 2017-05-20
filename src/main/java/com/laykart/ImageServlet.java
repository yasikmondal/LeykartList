package com.laykart;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

//@MultipartConfig
@SuppressWarnings("serial")
public class ImageServlet extends HttpServlet {

	final String bucketName = "laykart-165108.appspot.com";
	String imgPath = "https://storage.googleapis.com/laykart-165108.appspot.com/";

	// [START gcs]

	// Allows creating and accessing files in Google Cloud Storage.
	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
			.initialRetryDelayMillis(10).retryMaxAttempts(10).totalRetryPeriodMillis(15000).build());
	// [END gcs]

	/*
	 * @Override public void doPost(HttpServletRequest req, HttpServletResponse
	 * resp) throws ServletException,IOException {
	 * 
	 * 
	 * Part filePart = req.getPart("fileName"); // Retrieves <input type="file"
	 * name="file"> //String fileName =
	 * Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); //
	 * MSIE fix. //InputStream fileContent = filePart.getInputStream();
	 * 
	 * String newImageUrl = null; CloudStorageHelper cludhelp=new
	 * CloudStorageHelper(); newImageUrl = cludhelp.uploadFile( filePart ,
	 * bucket);
	 * 
	 * //doGet(req, resp); System.out.println(newImageUrl);
	 * 
	 * }
	 */

	public static Bucket getBucket(String bucketName) throws IOException, GeneralSecurityException {
		Storage client = StorageFactory.getService();

		Storage.Buckets.Get bucketRequest = client.buckets().get(bucketName);
		// Fetch the full set of the bucket's properties (e.g. include the ACLs
		// in the response)
		bucketRequest.setProjection("full");
		return bucketRequest.execute();
	}
	// [END get_bucket]

	public static List<StorageObject> listBucket(String bucketName) throws IOException, GeneralSecurityException {
		Storage client = StorageFactory.getService();
		Storage.Objects.List listRequest = client.objects().list(bucketName);

		List<StorageObject> results = new ArrayList<StorageObject>();
		Objects objects;

		// Iterate through each page of results, and add them to our results
		// list.
		do {
			objects = listRequest.execute();
			// Add the items in this page of results to the list we'll return.
			results.addAll(objects.getItems());

			// Get the next page, in the next iteration of this loop.
			listRequest.setPageToken(objects.getNextPageToken());
		} while (null != objects.getNextPageToken());

		return results;
	}
	// [END list_bucket]

	public static byte[] extractBytes(String ImageName) throws IOException {
		// open image
		String imgPath = "https://storage.googleapis.com/laykart-165108.appspot.com/" + ImageName;
		// File imgPath = new
		// File("https://storage.googleapis.com/laykart-165108.appspot.com/" +
		// ImageName);
		URL url = new URL(imgPath);
		// Image image = ImageIO.read(url);

		BufferedImage bufferedImage = ImageIO.read(url);

		// get DataBufferBytes from Raster
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

		return (data.getData());
	}

	@SuppressWarnings("resource")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		// Get metadata about the specified bucket.
		Bucket bucket;
		try {
			/*bucket = getBucket(bucketName);

			System.out.println("name: " + bucketName);
			System.out.println("location: " + bucket.getLocation());
			System.out.println("timeCreated: " + bucket.getTimeCreated());
			System.out.println("owner: " + bucket.getOwner());*/

			// List the contents of the bucket.
			List<StorageObject> bucketContents = listBucket(bucketName);
			if (null == bucketContents) {
				System.out.println("There were no objects in the given bucket; try adding some and re-running.");
			}

			for (StorageObject object : bucketContents) {

				if ("leyKart-images/B1/G1.png".equals(object.getName())) {
					byte[] imageBytes = null;
					if ("image/png".equals(object.getContentType())) {
						
						System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSS");
						System.out.println(object.getContentType());

						//imageBytes = extractBytes(object.getName());

						// [START original_image]
						// Read the image.jpg resource into a ByteBuffer.
						System.out.println(imgPath);
						ServletContext context = getServletContext();
						//URL resource = context.getResource("/WEB-INF/image.jpg");
						URL resource = context.getResource(imgPath + object.getName());
						File file = null;

						file = new File(resource.toURI());

						FileInputStream fileInputStream = new FileInputStream(file);
						FileChannel fileChannel = fileInputStream.getChannel();
						ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
						fileChannel.read(byteBuffer);

						imageBytes = byteBuffer.array();
						System.out.println("Test2");

						// Write the original image to Cloud Storage
						/*gcsService.createOrReplace(new GcsFilename(bucketName, "image.jpg"),
								new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
								ByteBuffer.wrap(imageBytes));*/
						// [END original_image]

						// [START resize]
						// Get an instance of the imagesService we can use to
						// transform
						// images.
						ImagesService imagesService = ImagesServiceFactory.getImagesService();

						// Make an image directly from a byte array, and
						// transform it.
						Image image = ImagesServiceFactory.makeImage(imageBytes);
						Transform resize = ImagesServiceFactory.makeResize(100, 50);
						Image resizedImage = imagesService.applyTransform(resize, image);

						System.out.println("----------------------------");
						System.out.println(resizedImage);

						// Write the transformed image back to a Cloud Storage
						// object.
						gcsService.createOrReplace(new GcsFilename(bucketName, "resizedImage_100X50.jpeg"),
								new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
								ByteBuffer.wrap(resizedImage.getImageData()));
						// [END resize]

						// [START rotate]
						// Make an image from a Cloud Storage object, and
						// transform it.

						// BlobstoreService allows you to manage the creation
						// and
						// serving of large, immutable blobs to users.
						System.out.println("Test3");
						//BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
						//BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/image.jpg"); // Creating
																												// a
																												// BlobKey
																												// for
																												// a
																												// Google
																												// Storage
																												// File.
						// BlobKey blobKey =
						// blobstoreService.createGsBlobKey("//storage.googleapis.com/"
						// + bucket + "/Test/unnamed.jpg");

						//Image blobImage = ImagesServiceFactory.makeImageFromBlob(blobKey); // Create
																							// an
																							// image
																							// backed
																							// by
																							// the
																							// specified
																							// blobKey.
						//Transform rotate = ImagesServiceFactory.makeRotate(90);
						//Image rotatedImage = imagesService.applyTransform(rotate, blobImage);

						// Write the transformed image back to a Cloud Storage
						// object.
						/*gcsService.createOrReplace(new GcsFilename(bucketName, "rotatedImage.jpeg"),
								new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
								ByteBuffer.wrap(rotatedImage.getImageData()));*/
					}
				}
			}
			// [END rotate]
			System.out.println("Test4");
			// Output some simple HTML to display the images we wrote to Cloud
			// Storage
			// in the browser.
			PrintWriter out = resp.getWriter();
			out.println("<html><body>\n");
			out.println("<img src='https://storage.googleapis.com/" + bucketName + "/image.jpeg' alt='AppEngine logo' />");
			out.println("<img src='https://storage.googleapis.com/" + bucketName
					+ "/resizedImage_google.jpeg' alt='AppEngine logo resized' />");
			out.println("<img src='https://storage.googleapis.com/" + bucketName
					+ "/rotatedImage_google.jpeg' alt='AppEngine logo rotated' />");
			out.println("</body></html>\n");

		} catch (GeneralSecurityException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}
}
