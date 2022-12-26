import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sped.core.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterTest {
    //classe usada para o teste addNamedRegisterTest
    public static class NamedRegisterTest extends NamedRegister {
        public NamedRegisterTest(Context context) {
            super(context, "C101");
        }
    }

    @Test
    @DisplayName("Testa a inclusão de Registro filho através do métodos addRegister")
    void addRegisterTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();

        //#1 — verifica se um registro adicionado via addRegister de fato está inserido nos registros filhos
        var rC100 = blockC.addRegister("C100");
        rC100.addRegister("C101");
        Counter counter = new Counter();
        rC100.count(counter);
        assertEquals(
                1,
                counter.count("C101"),
                "#1 Era esperado que o registro adicionado via método addRegister fosse considerado na contagem dos registros"
        );
    }

    @Test
    @DisplayName("Testa a inclusão de Registro filho através do métodos addNamedRegister")
    void addNamedRegisterTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();

        //#1 - verifica se um registro adicionado via addNamedRegister de fato está inserido nos registros filhos
        var rC100 = blockC.addRegister("C100");

        rC100.addNamedRegister(NamedRegisterTest.class);
        Counter counter = new Counter();
        rC100.count(counter);

        assertEquals(
                1,
                counter.count("C101"),
                "#1 Era esperado que o registro adicionado via método addNamedRegister fosse considerado na contagem dos registros"
        );
    }

    @Test
    @DisplayName("teste do método getFields()")
    void getFieldsTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();


        var rC100 = blockC.addRegister("C100");

        //#1 - verifica se os 28 fields definidos em definitions.xml serão importados
        assertEquals(
                28,
                rC100.getFields().size(),
                "#1 Deveria retornar 28 registros, conforme definitions.xml"
        );

        //#2 - verifica nome a nome se todos os fields definidos em definitions.xml podem ser encontrados
        List<String> fieldNames = Arrays.asList(
            "IND_OPER", "IND_EMIT", "COD_PART", "COD_MOD", "COD_SIT",
            "SER", "NUM_DOC", "CHV_NFE", "DT_DOC", "DT_E_S", "VL_DOC",
            "IND_PGTO", "VL_DESC", "VL_ABAT_NT", "VL_MERC", "IND_FRT",
            "VL_FRT", "VL_SEG", "VL_OUT_DA", "VL_BC_ICMS", "VL_ICMS",
            "VL_BC_ICMS_ST", "VL_ICMS_ST", "VL_IPI", "VL_PIS", "VL_COFINS",
            "VL_PIS_ST", "VL_COFINS_ST");

        fieldNames.forEach(fieldName -> {
            assertDoesNotThrow(
                    () -> rC100.getField(fieldName),
                    "#2 Field '%s' deveria ser encontrado mas não foi".formatted(fieldName)
            );
        });
    }

    @Test
    @DisplayName("teste do método toString()")
    void toStringTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();

        //#1 - verifica se o toString retorna todos os fields como esperado
        var rC100 = blockC.addRegister("C100");
        var toStringResult = rC100.toString();

        assertEquals(
                "|C100|||||||||||||||||||||||||||||",
                toStringResult,
                "#1 a saída de toString está diferente da esperada"
        );

        //#2 - certifica que os registros filhos não sejam retornados no toString
        rC100.addRegister("C101");
        var toStringResultAfterAddSonRegister = rC100.toString();

        assertEquals(
                toStringResult,
                toStringResultAfterAddSonRegister,
                "#2 após adicionar um registro filho, toString deve continuar com a mesma saída de antes"
        );
    }

    @Test
    @DisplayName("teste do método count(Counter counter)")
    void countWithCounterParameterTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();

        var rC100 = blockC.addRegister("C100");

        //#1 - Contagem de Registro sem registros filhos
        var counter1 = new Counter();
        rC100.count(counter1);
        assertEquals(
                1,
                counter1.count(),
                "#1 É esperado que um Registro sem Filhos retorne 1 ao contar"
        );

        //#2 - Contagem de Registro com 1 Registro filho
        var counter2 = new Counter();
        rC100.addRegister("C101");
        rC100.count(counter2);
        assertEquals(
                2,
                counter2.count(),
                "#2 É esperado que um Registro Com 1 Registro Filho retorne 2 ao contar"
        );

        //#3 - Contagem de Registro específico
        var counter3 = new Counter();
        rC100.addRegister("C101");
        rC100.count(counter3);
        assertEquals(
                2,
                counter3.count("C101"),
                "#2 É esperado que o contador retornasse 2 ao contar pelo registro específico"
        );
    }

    @Test
    @DisplayName("teste do método count()")
    void countTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();

        var rC100 = blockC.addRegister("C100");

        //#1 - Contagem de Registro sem registros filhos
        assertEquals(
                1,
                rC100.count(),
                "#1 É esperado que um Registro sem Filhos retorne 1 ao contar"
        );

        //#2 - Contagem de Registro com 1 Registro filho
        rC100.addRegister("C101");

        assertEquals(
                2,
                rC100.count(),
                "#2 É esperado que um Registro Com 1 Registro Filho retorne 2 ao contar"
        );

        //#3 - Contagem de Registro com 2 Registros Filhos
        rC100.addRegister("C101");

        assertEquals(
                3,
                rC100.count(),
                "#3 É esperado que um Registro Com 2 Registros Filhos retorne 3 ao contar"
        );

        //#4 - Contagem de Registro com Registros Filhos que também contém Registros Filhos
        var rC110 = rC100.addRegister("C110");
        rC110.addRegister("C111");

        assertEquals(
                5, //c100 + 2xC101 + C110 + C111
                rC100.count(),
                "#4 Falha ao contar registro com registros filhos que também tem registros filhos"
        );
    }

    @Test @DisplayName("teste do método write()")
    void writeTest() {
        var spedGenerator = SpedGenerator.newBuilder("definitions.xml")
                .setFileLoader(fileName -> Objects.requireNonNull(SpedGeneratorTest.class.getResourceAsStream(fileName)))
                .build();

        var blockC = spedGenerator.newBlockBuilder()
                .setBlockName("C")
                .setOpeningRegisterName("C001")
                .setClosureRegisterName("C990")
                .build();


        var rC100 = blockC.addRegister("C100");

        //#1 — verifica se o registro c100 irá escrever - sem valores atribuidos aos fields
        AtomicReference<String> rC100Write = new AtomicReference<>();
        rC100.write((string, register) -> rC100Write.set(string));

        assertEquals(
                "|C100|||||||||||||||||||||||||||||",
                rC100Write.toString(),
                "#1 a saída de write() está diferente da esperada"
        );

        //#2 — verifica se o registro c100 irá escrever - com valores atribuidos aos fields
        List<String> fieldNames = Arrays.asList(
                "IND_OPER", "IND_EMIT", "COD_PART", "COD_MOD", "COD_SIT",
                "SER", "NUM_DOC", "CHV_NFE", "DT_DOC", "DT_E_S", "VL_DOC",
                "IND_PGTO", "VL_DESC", "VL_ABAT_NT", "VL_MERC", "IND_FRT",
                "VL_FRT", "VL_SEG", "VL_OUT_DA", "VL_BC_ICMS", "VL_ICMS",
                "VL_BC_ICMS_ST", "VL_ICMS_ST", "VL_IPI", "VL_PIS", "VL_COFINS",
                "VL_PIS_ST", "VL_COFINS_ST");

        Date dtDoc = null;
        Date dtES = null;
        try {
            dtDoc = new SimpleDateFormat("dd/MM/yyyy").parse("01/12/2022");
            dtES = new SimpleDateFormat("dd/MM/yyyy").parse("02/12/2022");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        try {
            //fiedls setados com dados inválidos, apenas para testar a saida dos dados
            //e se todos os fields estão saindo no write.
            rC100.setFieldValue("IND_OPER", "A");
            rC100.setFieldValue("IND_EMIT", "B");
            rC100.setFieldValue("COD_PART", "C");
            rC100.setFieldValue("COD_MOD", "D");
            rC100.setFieldValue("COD_SIT", "E");
            rC100.setFieldValue("SER", "F");
            rC100.setFieldValue("NUM_DOC", "1");
            rC100.setFieldValue("CHV_NFE", "99999");
            rC100.setFieldValue("DT_DOC", dtDoc);
            rC100.setFieldValue("DT_E_S", dtES);
            rC100.setFieldValue("VL_DOC", 2.0);
            rC100.setFieldValue("IND_PGTO", "H");
            rC100.setFieldValue("VL_DESC", 3.0);
            rC100.setFieldValue("VL_ABAT_NT", 4.0);
            rC100.setFieldValue("VL_MERC", 5.0);
            rC100.setFieldValue("IND_FRT", "I");
            rC100.setFieldValue("VL_FRT", 6.0);
            rC100.setFieldValue("VL_SEG", 7.0);
            rC100.setFieldValue("VL_OUT_DA", 8.0);
            rC100.setFieldValue("VL_BC_ICMS", 9.0);
            rC100.setFieldValue("VL_ICMS", 10.0);
            rC100.setFieldValue("VL_BC_ICMS_ST", 11.0);
            rC100.setFieldValue("VL_ICMS_ST", 12.0);
            rC100.setFieldValue("VL_IPI", 13.0);
            rC100.setFieldValue("VL_PIS", 14.0);
            rC100.setFieldValue("VL_COFINS", 15.0);
            rC100.setFieldValue("VL_PIS_ST", 16.0);
            rC100.setFieldValue("VL_COFINS_ST", 17.0);
        } catch (FieldNotFoundException e) {
            throw new RuntimeException(e);
        }

        AtomicReference<String> rC100Write2 = new AtomicReference<>();
        rC100.write((string, register) -> rC100Write2.set(string));

        assertEquals(
                "|C100|A|B|C|D|E|F|1|99999|01122022|02122022|2,00|H|3,00|4,00|5,00|I|6,00|7,00|8,00|9,00|10,00|11,00|12,00|13,00|14,00|15,00|16,00|17,00|",
                rC100Write2.toString(),
                "#2 a saída de write() está diferente da esperada"
        );

        //#3 — checa se o write está retornado o registro
        AtomicReference<Register> registerRetornedFromWrite = new AtomicReference<>();
        rC100.write((string, register) -> registerRetornedFromWrite.set(register));


        assertEquals(
                rC100,
                registerRetornedFromWrite.get(),
                "#3 O objeto (Register) retornado pelo write não é o esperado"
        );

        //#4 - teste do método write ao adicionar registros filhos
        rC100.addRegister("C101");

        AtomicInteger countWrite= new AtomicInteger();
        rC100.write((string, register) -> countWrite.getAndIncrement());

        assertEquals(
                2,
                countWrite.get(),
                "#4 Era esperado a escrita de dois Registers"
        );
    }
}
