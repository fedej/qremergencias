package ar.com.utn.proyecto.qremergencias.bo.controller;

import ar.com.utn.proyecto.qremergencias.bo.service.VerificationService;
import ar.com.utn.proyecto.qremergencias.core.domain.DoctorFront;
import com.mongodb.gridfs.GridFSDBFile;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

@Controller
@RequestMapping("/verification")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@Log
public class VerificationController {

    private static final String USER_INDEX = "verification/index";

    private final VerificationService verificationService;

    @Autowired
    public VerificationController(final VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/index")
    public String list(final Model model, final Pageable page) {
        final Page<DoctorFront> users = verificationService.findMedicos(page);
        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

    @PostMapping("/verify")
    public String verify(@RequestParam final String id, final Model model, final Pageable page) {
        verificationService.verify(id);
        final Page<DoctorFront> users = verificationService.findMedicos(page);
        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

    @PostMapping("/unverify")
    public String unverify(@RequestParam final String id, final Model model, final Pageable page) {
        verificationService.unverify(id);
        final Page<DoctorFront> users = verificationService.findMedicos(page);
        model.addAttribute("page", users);
        model.addAttribute("rolesList", Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        return USER_INDEX;
    }

    @PostMapping("/download")
    @ResponseBody
    public void downloadEvidenceFile(@RequestParam final String id, HttpServletResponse response) {
        GridFSDBFile file = verificationService.downloadEvidenceFile(id);
        if (file != null) {
            try {
                response.setContentType(file.getContentType());
                response.setContentLength(new Integer(String.valueOf(file.getLength())));
                //response.setContentLength(Integer.valueOf(String.valueOf(file.getLength())));
                response.setHeader("Content-Disposition", "attachment; filename=" + file.getFilename());
                copyStream(file.getInputStream(), response.getOutputStream());
            } catch (IOException ex) {
                throw new RuntimeException("IOError writing file to output stream");
            }
        }
        log.info("Download Evidence File...");
    }

    public static long copyStream(InputStream input, OutputStream output) throws IOException {
        try (
                ReadableByteChannel inputChannel = Channels.newChannel(input);
                WritableByteChannel outputChannel = Channels.newChannel(output);
        ) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
            long size = 0;

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }

            return size;
        }
    }

}
