package gov.gsa.samplesp;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.io.IOException;
import java.lang.System;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.encryption.DecryptionException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@EnableAutoConfiguration
public class App {

  public static void main(String[] args) {
    try {
      DefaultBootstrap.bootstrap();
    } catch (ConfigurationException e) {
      System.err.println("DefaultBootstrap.bootstrap failed");
      return;
    }
    SpringApplication.run(App.class, args);
  }

  private XMLObjectBuilder getBuilder(QName name) {
    return Configuration.getBuilderFactory().getBuilder(name);
  }

  private Issuer buildIssuer() {
    @SuppressWarnings("unchecked")
    SAMLObjectBuilder<Issuer> issuerBuilder = (SAMLObjectBuilder<Issuer>) getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
    Issuer issuer = issuerBuilder.buildObject();
    issuer.setValue("urn:gov:gsa:SAML:2.0.profiles:sp:sso:localhost");
    return issuer;

  }

  private AuthnRequest buildAuthnRequest() {
    @SuppressWarnings("unchecked")
    SAMLObjectBuilder<AuthnRequest> authn_builder = (SAMLObjectBuilder<AuthnRequest>) getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
    AuthnRequest req = authn_builder.buildObject();
    req.setIsPassive(true);
    req.setVersion(SAMLVersion.VERSION_20);
    req.setAssertionConsumerServiceURL("http://localhost:4567/consume");
    req.setProtocolBinding("HTTP_POST");
    req.setRequestedAuthnContext(buildAuthnContext());
    req.setIssuer(buildIssuer());

    return req;
  }

  private RequestedAuthnContext buildAuthnContext() {
    @SuppressWarnings("unchecked")
    SAMLObjectBuilder<AuthnContextClassRef> contextRefBuilder = (SAMLObjectBuilder<AuthnContextClassRef>) getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
    AuthnContextClassRef classRef = contextRefBuilder.buildObject();
    classRef.setAuthnContextClassRef("http://idmanagement.gov/ns/assurance/loa/1");

    @SuppressWarnings("unchecked")
    SAMLObjectBuilder<RequestedAuthnContext> builder = (SAMLObjectBuilder<RequestedAuthnContext>) getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
    RequestedAuthnContext authnContext = builder.buildObject();
    authnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
    authnContext.getAuthnContextClassRefs().add(classRef);
    return authnContext;
  }

  private Endpoint getIDPEndpoint() {
    @SuppressWarnings("unchecked")
    SAMLObjectBuilder<Endpoint> builder = (SAMLObjectBuilder<Endpoint>) getBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME);
    Endpoint samlEndpoint = builder.buildObject();
    samlEndpoint.setLocation("http://localhost:3000/api/saml/auth");
    samlEndpoint.setResponseLocation("http://bar.com");
    return samlEndpoint;
  }

  @RequestMapping("/login")
  String login(HttpServletResponse response) throws IOException, MessageEncodingException {
    AuthnRequest req = buildAuthnRequest();

    BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
    HttpServletResponseAdapter transport = new HttpServletResponseAdapter(response, false);
    context.setOutboundMessageTransport(transport);
    context.setPeerEntityEndpoint(getIDPEndpoint());
    context.setOutboundSAMLMessage(req);

    System.out.println("req = " + req);
    HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
    encoder.encode(context);
    System.out.println("location = " + response.getHeader("location"));
    return null;
  }

  private SAMLMessageContext extractSAMLMessageContext(HttpServletRequest request) throws MessageDecodingException, SecurityException {
    BasicSAMLMessageContext messageContext = new BasicSAMLMessageContext();

    messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));
    //messageContext.setSecurityPolicyResolver(resolver);

    HTTPPostDecoder decoder = new HTTPPostDecoder();
    decoder.decode(messageContext);
    return messageContext;
  }

  @RequestMapping(value="/consume", method=RequestMethod.POST)
    String consume(HttpServletRequest request, HttpServletResponse response,
        @RequestParam(value="SAMLResponse", required=true) String samlResponseString) throws MessageDecodingException, SecurityException {
      System.out.println("SAMLResponseString!!!! " + samlResponseString.length());
      SAMLMessageContext messageContext = extractSAMLMessageContext(request);

      Response samlResponse = (Response) messageContext.getInboundSAMLMessage();
      String statusCode = samlResponse.getStatus().getStatusCode().getValue();
      if (!StatusCode.SUCCESS_URI.equals(statusCode)) {
        System.out.println("SAML Logon failed: " + statusCode);
        return "redirect:/failure";
      }

      List<Assertion> assertionList = samlResponse.getAssertions();

      // Decrypt assertions
      if (samlResponse.getEncryptedAssertions().size() > 0) {
          Credential encryptionCredential = null;

          KeyInfoCredentialResolver resolver = new StaticKeyInfoCredentialResolver(encryptionCredential);
          Decrypter decrypter = new Decrypter(null, resolver, null);
          decrypter.setRootInNewDocument(true);

          assertionList = new ArrayList<Assertion>(samlResponse.getAssertions().size() + samlResponse.getEncryptedAssertions().size());
          assertionList.addAll(samlResponse.getAssertions());
          List<EncryptedAssertion> encryptedAssertionList = samlResponse.getEncryptedAssertions();
          for (EncryptedAssertion ea : encryptedAssertionList) {
              try {
                  System.out.println("Decrypting assertion");

                  Assertion decryptedAssertion = decrypter.decrypt(ea);
                  assertionList.add(decryptedAssertion);
              } catch (DecryptionException e) {
                  System.out.println("Decryption of received assertion failed, assertion will be skipped");
              }
          }
      }

      for (Assertion assertion : assertionList) {
        System.out.println("assertion: " + assertion);
      }
      String email = "goobar";
      response.addCookie(new Cookie("email", email));
      return "redirect:/success";
    }

  @RequestMapping("/success")
  String success(Model model,
      @CookieValue(value="email") String email_cookie) {
    model.addAttribute("email", email_cookie);
    return "success";
  }

  @RequestMapping("/")
  String root(Model model) {
    model.addAttribute("intstream", IntStream.range(0, 6).iterator());
    return "index";
  }
}
