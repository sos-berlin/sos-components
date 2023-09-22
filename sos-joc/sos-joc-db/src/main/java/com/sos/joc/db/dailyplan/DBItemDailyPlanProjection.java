package com.sos.joc.db.dailyplan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.sos.commons.util.SOSStreamUnzip;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_DPL_PROJECTIONS)
public class DBItemDailyPlanProjection extends DBItem {

    private static final long serialVersionUID = 1L;

    public static final Long METADATEN_ID = 0L;
    private static final int BUFFER = 4096;

    @Id
    @Column(name = "[ID]")
    private Long id;

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "[CONTENT]", nullable = false)
    private byte[] content;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public byte[] getContent() throws IOException {
        return SOSStreamUnzip.unzip(content, BUFFER);
    }

    public void setContent(byte[] val) throws IOException {
        if (val.length >= 4 && val[0] == (byte) 0x1f && val[1] == (byte) 0x8b) {
            content = val;
        } else {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); InputStream is = new ByteArrayInputStream(val); GZIPOutputStream gos =
                    new GZIPOutputStream(bos)) {
                byte[] buffer = new byte[BUFFER];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    gos.write(buffer, 0, len);
                }
                gos.close();
                content = bos.toByteArray();
            }
        }
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    @Transient
    public boolean isMeta() {
        return id != null && id.equals(METADATEN_ID);
    }

}