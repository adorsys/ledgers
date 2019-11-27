
package de.adorsys.ledgers.deposit.api.domain.exchange;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.example.demo.feignTest.tststst package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Cube_QNAME = new QName("http://www.ecb.int/vocabulary/2002-08-01/eurofxref", "Cube");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CubeType }
     *
     * @return CubeType
     */
    public CubeType createCubeType() {
        return new CubeType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CubeType }{@code >}}
     *
     * @param value value
     * @return JAXBElement of CubeType
     */
    @XmlElementDecl(namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref", name = "Cube")
    public JAXBElement<CubeType> createCube(CubeType value) {
        return new JAXBElement<>(_Cube_QNAME, CubeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CubeType }{@code >}}
     *
     * @param value value
     * @return JAXBElement of CubeType
     */
    @XmlElementDecl(namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref", name = "Cube", scope = CubeType.class)
    public JAXBElement<CubeType> createCubeTypeCube(CubeType value) {
        return new JAXBElement<>(_Cube_QNAME, CubeType.class, CubeType.class, value);
    }

}
