package org.domain.financial2.rest;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.domain.financial2.beans.LogCrackerBean;
import org.domain.financial2.beans.LogImporterBean;
import org.domain.financial2.messages.capture.CaptureTables;

@Stateless
@Path("/cracker")
public class CrackerEndpoint implements Serializable {
	private static final long serialVersionUID = -4923731241679521330L;
	private Logger logger;

	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

//	@PersistenceContext(unitName = "FinancialSmall")
//	EntityManager entityManagerSmall;

	@Inject
	LogCrackerBean logCracker;

	@Inject
	LogImporterBean logImporter;

	private CaptureTables captureTables;

	@PostConstruct
	void postConstruct() {
		this.logger = Logger.getLogger("financialWeb");
		this.captureTables = new CaptureTables(this.entityManager, true);
	}

	@GET
	@Produces("application/json")
	public Response listAll() {
		this.logger.log(Level.INFO, ">>>>>>> crarckerStatus <<<<<<<<<<");
		Integer state = 0;//this.manager.getManager().getState();
		return Response.ok(state).build();
	}

	@POST
	@Path("generateTables")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String generateTables(@QueryParam("type") String type) {
		this.logger.log(Level.INFO, String.format(">>>>>>> generateTables : type = %s", type));
		String textOut = "";
		
		if (type != null && type.equals("chipPOS")) {
			textOut = this.captureTables.generateTable_posFrete_950032();
		} else if (type != null && type.equals("chipTEF")) {
			textOut = this.captureTables.generateTable_tefHct_920000();
		}
		
		return textOut;
	}

	@POST
	@Path("crack")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String crack(@QueryParam("store") Boolean store, @QueryParam("clear") Boolean clear, @QueryParam("type") String type, String textIn) {
		this.logger.log(Level.INFO, String.format(">>>>>>> crack : store = %s, clear = %s, type = %s, textIn = %s", store, clear, type, textIn));
		
		if (store == null) {
			store = false;
		}
		
		if (clear != null && clear == true) {
			this.logCracker.clearDb();
//			this.logImporter.clearDb();
		}
		
		String textOut = "";
		
		if (type != null && type.equals("chipPOS")) {
			this.captureTables.setEntityManager(this.entityManager);
			textOut = this.captureTables.parse(textIn, "950032", clear);
		} else if (type != null && type.equals("chipTEF")) {
			this.captureTables.setEntityManager(this.entityManager);
			textOut = this.captureTables.parse(textIn, "920000", clear);
		} else {
			textOut = this.logCracker.crack(textIn, store);
		}
		
		return textOut;
	}

	@POST
	@Path("logConverter")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String logConverter() {
		this.logger.log(Level.INFO, ">>>>>>> logConverter <<<<<<<<<<");
		String logs = this.logImporter.popLogs();
//		this.logger.log(Level.INFO, logs);
		return logs;
	}

	@GET
	@Path("startConverter")
	public Response startConverter(@QueryParam("clearDb") Boolean clearDb) {
		this.logger.log(Level.INFO, ">>>>>>> startConverter <<<<<<<<<<");
		boolean keepRunning = false;
		
		if (clearDb == null) {
			clearDb = false;
		}
		
		this.logImporter.importFiles(keepRunning, clearDb, true, null);
		return Response.ok("Processed").build();
	}

	@GET
	@Path("stopConverter")
	public Response stopConverter() {
		this.logger.log(Level.INFO, ">>>>>>> stopConverter <<<<<<<<<<");
		return Response.ok("Processed").build();
	}

//	@POST
//	@Path("/upload")
//	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
//	@Produces(MediaType.TEXT_PLAIN)
//	public String uploadFile(byte[] data) {
//		this.logger.log(Level.INFO, ">>>>>>> uploadFile <<<<<<<<<<");
//		
//		try {
//			String fileName = "teste.dat";
//			FileOutputStream fos = new FileOutputStream(fileName);
//			fos.write(data);
//			fos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		String textIn = new String(data);
//		String textOut = this.logCracker.crack(textIn, true);
//		return textOut;
//	}
	
//	public void saveFile(InputStream is, String fileName) throws Exception {
//		FileOutputStream fos = new FileOutputStream(fileName);
//		byte[] buffer = new byte[100*1024];
//		int size;
//		
//		while ((size = is.read(buffer)) > 0) {
//			fos.write(buffer, 0, size);
//		}
// 
//		fos.close();
//	}
//	
//	@POST
//	@Path("/upload")
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.TEXT_PLAIN)
//	public Response uploadFile(MultipartFormDataInput input) {
//		this.logger.log(Level.INFO, ">>>>>>> uploadFile <<<<<<<<<<");
//		String fileName = "unknown";
//		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
//		List<InputPart> inputParts = uploadForm.get("uploadedFile");
//	
//		for (InputPart inputPart : inputParts) {
//			try {
//				MultivaluedMap<String, String> header = inputPart.getHeaders();
//		
//				{
//					String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
//		
//					for (String filenameIt : contentDisposition) {
//						if (filenameIt.trim().startsWith("filename")) {
//							String[] name = filenameIt.split("=");
//							fileName = name[1].trim().replaceAll("\"", "");
//						}
//					}
//				}
//				// convert the uploaded file to inputstream
//				InputStream inputStream = inputPart.getBody(InputStream.class, null);
//				saveFile(inputStream, fileName);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	
//		}
//		// String textOut = this.logCracker.crack(textIn, true);
//		return Response.status(200).entity("uploadFile is called, Uploaded file name : " + fileName).build();
//	}
}
