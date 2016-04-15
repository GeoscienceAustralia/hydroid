package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.dto.MenuDTO;
import au.gov.ga.hydroid.service.JenaService;
import au.gov.ga.hydroid.utils.IOUtils;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by u24529 on 15/04/2016.
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

   @Autowired
   private JenaService jenaService;

   @RequestMapping(value = "/hydroid", method = {RequestMethod.GET})
   public @ResponseBody ResponseEntity<List<MenuDTO>> enhance() {

      List<MenuDTO> menu = new ArrayList<MenuDTO>();

      InputStream inputStream = getClass().getResourceAsStream("/hydroid.rdf");
      String rdfContent =  new String(IOUtils.fromInputStreamToByteArray(inputStream));
      List<Statement> statements = jenaService.parseRdf(rdfContent, "");

      // iterate over statements, etc...

      return new ResponseEntity<>(menu, HttpStatus.OK);
   }

}