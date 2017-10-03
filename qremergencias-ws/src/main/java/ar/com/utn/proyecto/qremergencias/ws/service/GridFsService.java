package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import com.mongodb.gridfs.GridFSDBFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class GridFsService {

    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public GridFsService(final GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public Resource findFileById(final String fileId) {
        final GridFSDBFile file = findGridFSFile().apply(fileId);

        if (file != null) {
            return new InputStreamResource(file.getInputStream());
        }

        return null;
    }

    public Function<String, GridFSDBFile> findGridFSFile() {
        return (fileId) -> gridFsTemplate.findOne(new Query()
                .addCriteria(where("_id").is(new ObjectId(fileId))));
    }

    public Object saveQRImage(final UserFront user, final BufferedImage image) {

        if (user.getQr() != null) {
            deleteQR(user);
        }

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpeg", os);
            try (final InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                return gridFsTemplate.store(is, user.getId() + "/QR.jpeg",
                        MimeTypeUtils.IMAGE_JPEG_VALUE).getId();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteQR(final UserFront userFront) {
        gridFsTemplate.delete(new Query(where("_id").is(userFront.getQr())));
    }
}
