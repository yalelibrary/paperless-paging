package edu.yale.library.paperless.entities;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import net.coobird.thumbnailator.Thumbnails;

@Entity
@Getter
@Setter
public class Avatar extends BaseEntity{

    @Transient
    private Logger logger = LoggerFactory.getLogger(getClass());

    @OneToOne
    private User user;

    @Lob
    private byte[] image;

    private String mimeType;

    public void setFromDataUrl(String dataUrl) {
        String s[] = dataUrl.split(";");
        if ( s[0].startsWith("data:") ) {
            mimeType = s[0].substring("data:".length());
        }
        String dataUrlContent = s[s.length-1];
        if ( dataUrlContent.startsWith("base")) {
            dataUrlContent = dataUrlContent.substring(dataUrlContent.indexOf(',')+1);
        }
        this.image = Base64.getDecoder().decode(dataUrlContent);
        try {
            attemptImageResize();
        } catch (Exception e) {
            logger.error("Unable to resize image");
        }
    }

    private void attemptImageResize() throws IOException {
        long size = this.image.length;
        if ( size < 5000 ) {
            logger.info("Not resizing small image");
            return;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(this.image);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .width(100)
                .toOutputStream(outputStream);
        this.image = outputStream.toByteArray();
        logger.info("Reduced size from " + size + " to " + this.image.length);
    }

}
