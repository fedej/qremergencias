package ar.com.utn.proyecto.qremergencias.util;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Pathology;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class QRUtils {

    private static final String CHARSET_NAME = "ISO-8859-1";
    public static final String TIPO_OTRO = "otro";

    private QRUtils() {

    }

    private static byte getSex(final char sex) {
        switch (sex) {
            case 'M':
                return 0b00;
            case 'F':
                return 0b01;
            default:
                return 0b10;
        }
    }

    private static byte getBlood(final String blood) {
        switch (blood) {
            case "0-":
                return 0b000;
            case "0+":
                return 0b001;
            case "A-":
                return 0b010;
            case "A+":
                return 0b011;
            case "B-":
                return 0b100;
            case "B+":
                return 0b101;
            case "AB-":
                return 0b110;
            case "AB+":
                return 0b111;
            default:
                return 0;
        }
    }

    @SuppressWarnings("PMD")
    public static byte[] encode(final EmergencyData emergencyData) throws UnsupportedEncodingException {
        final GeneralData general = emergencyData.getGeneral();
        final List<String> patos = new ArrayList<>();
        for (Pathology patho: emergencyData.getPathologies()) {
            if (patho.getType().equals(TIPO_OTRO)) {
                patos.add(patho.getDescription());
            } else {
                patos.add(patho.getType());
            }
        }

        final UserFront user = emergencyData.getUser();

        // Byte 0 y 1 Anio de nacimiento, sexo y sangre
        final ByteBuffer yearSexBloodBuffer = ByteBuffer.allocate(2).putShort((short) user.getBirthdate().getYear());
        int sex = QRUtils.getSex(user.getSex()) << 3;
        int blood = QRUtils.getBlood(general.getBloodType()) << 5;
        yearSexBloodBuffer.put(0, (byte) (yearSexBloodBuffer.get(0) | sex | blood));
        yearSexBloodBuffer.rewind();

        final String url = emergencyData.getUuid();
        final byte[] message = new byte[3 + url.length() + 1];
        System.arraycopy(yearSexBloodBuffer.array(), 0, message, 0, 2);

        // Byte 2 Alergias y patologias comunes
        final BitSet bitSet = new BitSet();
        if (general.getAllergies().contains("penicilina")) {
            bitSet.set(0);
        }
        if (general.getAllergies().contains("insulina")) {
            bitSet.set(1);
        }
        if (general.getAllergies().contains("rayos_x_con_yodo")) {
            bitSet.set(2);
        }
        if (general.getAllergies().contains("sulfamidas")) {
            bitSet.set(3);
        }
        if (patos.contains("hipertension")) {
            bitSet.set(4);
        }
        if (patos.contains("asma")) {
            bitSet.set(5);
        }
        if (patos.contains("antecedentes_oncologicos")) {
            bitSet.set(6);
        }
        if (patos.contains("insuficiencia_suprarrenal")) {
            bitSet.set(7);
        }
        final ByteBuffer allergiesAndPathosBuffer = ByteBuffer.allocate(1).put(bitSet.toByteArray());
        System.arraycopy(allergiesAndPathosBuffer.array(), 0, message, 2, 1);


        final byte[] urlBytes = url.getBytes(CHARSET_NAME);
        message[3] = (byte) urlBytes.length;
        System.arraycopy(urlBytes, 0, message, 4, urlBytes.length);

        if (user.getContacts() != null && !user.getContacts().isEmpty()) {
            UserEmergencyContact contact = user.getContacts().get(0);

            final byte[] nameBytes = contact.getFirstName().getBytes(CHARSET_NAME);
            final byte[] phoneBytes = contact.getPhoneNumber().getBytes(CHARSET_NAME);
            final byte[] contacts = new byte[nameBytes.length + phoneBytes.length + 2];

            contacts[0] = (byte) nameBytes.length;
            System.arraycopy(nameBytes, 0, contacts, 1, nameBytes.length);

            contacts[nameBytes.length + 1] = (byte) phoneBytes.length;
            System.arraycopy(phoneBytes, 0, contacts, nameBytes.length + 2, phoneBytes.length);

            final byte[] result = new byte[message.length + contacts.length];
            System.arraycopy(message, 0, result, 0, message.length);
            System.arraycopy(contacts, 0, result, message.length, contacts.length);
            return result;
        }


        return message;
    }

}