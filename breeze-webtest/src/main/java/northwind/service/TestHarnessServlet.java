package northwind.service;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.breeze.webserver.ControllerServlet;

public class TestHarnessServlet extends ControllerServlet {
    private static final long serialVersionUID = 1L;
    private static final int BUFFER_SIZE = 4096;
    private String _testCaseDir;

    @Override
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        _testCaseDir = config.getInitParameter("testCaseDir");
	}
	

	@Override
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		
		if (pathInfo == null || "/".equals(pathInfo)) pathInfo = "Index.hibernate.html";
		
		writeFileTo(pathInfo, response);
	}


    private void writeFileTo(String fileName, HttpServletResponse response) {
		
	    FileInputStream fileIn = null;
	    ServletOutputStream out = null;
		try {
		    
	        File file = new File(_testCaseDir, URLDecoder.decode(fileName, "UTF-8"));
	        if (!file.exists()) {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
	            return;
	        }		    
	        String contentType = getServletContext().getMimeType(file.getName());
	        if (contentType == null) {
	            contentType = "application/octet-stream";
	        }
	        response.setBufferSize(BUFFER_SIZE);
	        response.setContentType(contentType);
	        response.setHeader("Content-Length", String.valueOf(file.length()));
	        
			fileIn = new FileInputStream(file);
			out = response.getOutputStream();
	     
		    byte[] outputByte = new byte[BUFFER_SIZE];
		    int x;

		    while((x = fileIn.read(outputByte, 0, BUFFER_SIZE)) != -1) 	    {
		    	out.write(outputByte, 0, x);
		    }
		    out.flush();
		} catch (Exception e) {
			throw new RuntimeException("Unable to read: " + fileName, e);
		} finally {
		    close(fileIn);
		    close(out);
		}
    }
    
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    
	
}
