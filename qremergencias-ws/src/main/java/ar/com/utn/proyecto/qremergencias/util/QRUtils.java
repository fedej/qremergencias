package ar.com.utn.proyecto.qremergencias.util;

import ar.com.utn.proyecto.qremergencias.core.domain.UserEmergencyContact;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.EmergencyData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.GeneralData;
import ar.com.utn.proyecto.qremergencias.core.domain.emergency.Pathology;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

public final class QRUtils {

    private static final String CHARSET_NAME = "ISO-8859-1";

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
        final List<String> patos = emergencyData.getPathologies()
                .stream().map(Pathology::getDescription).collect(toList());

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
        final BitSet bitSet = BitSet.valueOf(message);
        if (general.getAllergies().contains("Penicilina")) {
            bitSet.set(16);
        }
        if (general.getAllergies().contains("Insulina")) {
            bitSet.set(17);
        }
        if (general.getAllergies().contains("Rayos X con yodo")) {
            bitSet.set(18);
        }
        if (general.getAllergies().contains("Sulfamidas")) {
            bitSet.set(19);
        }
        if (patos.contains("Hipertension")) {
            bitSet.set(20);
        }
        if (patos.contains("Asma")) {
            bitSet.set(21);
        }
        if (patos.contains("Antecedentes Oncologicos")) {
            bitSet.set(22);
        }
        if (patos.contains("Insuficiencia Suprarrenal")) {
            bitSet.set(23);
        }

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
