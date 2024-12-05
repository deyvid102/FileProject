package br.com.api.produtos.controle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.api.produtos.modelo.ProdutoModelo;
import br.com.api.produtos.modelo.RespostaModelo;
import br.com.api.produtos.repositorio.ProdutoRepositorio;
import br.com.api.produtos.servico.ProdutoServico;
import jakarta.servlet.http.HttpServletRequest;



@RestController
@CrossOrigin(origins = "*")

//ROTA DO ARQUIVO
public class ProdutoControle {
    private final Path caminhoArquivo;
    
    public ProdutoControle(ProdutoModelo ProdutoModelo){
        this.caminhoArquivo = Paths.get(ProdutoModelo.getUpload_dir())
        .toAbsolutePath().normalize();
    }

    @Autowired
    private ProdutoServico ps;
    @Autowired
    private ProdutoRepositorio pr;

    //DELETAR
    @DeleteMapping("/remover/{codigo}")
        public ResponseEntity<RespostaModelo> remover(@PathVariable Long codigo){
            ProdutoModelo selecionar =  pr.findByCodigo(codigo);
            String obj = selecionar.getUpload_dir();
            String objCut = obj.substring(22, obj.length() -0);
            String objRep = objCut.replace("%5C", "\\");
            System.out.println(objRep);

            File theDir = new File(objRep);
            theDir.delete();
            return ps.remover(codigo);
        }
    
    //ALTERAR
    @CrossOrigin
    @PutMapping(value = "/alterar" ,consumes = { MediaType.APPLICATION_JSON_VALUE , MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> alterar(@RequestPart(value = "nome",required = false) String nome, @RequestParam(value = "upload_dir",required = false) MultipartFile upload_dir, @RequestPart(value = "codigo",required = false) String codigo){
        
        pr.findByCodigo(Long.valueOf(codigo).longValue());
        String fileName = StringUtils.cleanPath(upload_dir.getOriginalFilename());
        Long x = Long.valueOf(codigo).longValue();
        LocalDate myObj = LocalDate.now();
        String ano = myObj.toString().substring(0,4);
        String mes = myObj.toString().substring(5,7);
        String dia = myObj.toString().substring(8,10);
        String rota = "C:\\Users\\folha\\Documents\\ESTUDOS\\PROJETOS\\BLOG\\produtos\\uploads\\" + ano + "\\" + mes + "\\" + dia + "\\";
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            
            .path(rota)
            .path(fileName)
            .toUriString();
        
        File theDir = new File(rota);
        ProdutoModelo selecionar =  pr.findByCodigo(x);
        String obj = selecionar.getUpload_dir();
        String objCut = obj.substring(22, obj.length() -0);
        String objRep = objCut.replace("%5C", "\\");
        String objRep2 = objRep.replace("%20", " ");
        File theDir2 = new File(objRep2);
        
        if(theDir2.exists()){
            
            try {
            theDir2.delete();
            } 
            
            catch(SecurityException se){
                System.out.println(se);
            }
        }
       System.out.println(fileDownloadUri + "funcionando" );
       System.out.println(theDir);
        if (!theDir.exists()) {
            try{
                theDir.mkdirs();
                
                

            }
            catch(SecurityException se){
                System.out.println(se);
            }

        }
        try {
            Path targetLocation = Paths.get(rota).resolve(fileName);
                System.out.println(targetLocation+" "+upload_dir);
                upload_dir.transferTo(targetLocation);

                ProdutoModelo teste = new ProdutoModelo(x, nome,fileDownloadUri);
                ps.cadastrarAlterar(teste, "alterar");

                return ResponseEntity.ok("Upload completo! Download link: " + fileDownloadUri + "  "+upload_dir);
            
        } catch (Exception e) {
            System.out.println(e);
        }

        return ResponseEntity.ok("");
            
         
    }
    

    //CADASTRAR
    @CrossOrigin
    @PostMapping(value = "/upload",consumes = { MediaType.APPLICATION_JSON_VALUE , MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> uploadFile(@RequestPart(value = "nome",required = false) String nome, @RequestParam(value = "upload_dir",required = false) MultipartFile upload_dir ){

        String fileName = StringUtils.cleanPath(upload_dir.getOriginalFilename());
        LocalDate myObj = LocalDate.now();
        String ano = myObj.toString().substring(0,4);
        String mes = myObj.toString().substring(5,7);
        String dia = myObj.toString().substring(8,10);
        
        String rota = "C:\\Users\\folha\\Documents\\ESTUDOS\\PROJETOS\\BLOG\\produtos\\uploads\\" + ano + "\\" + mes + "\\" + dia + "\\";

        File theDir = new File(rota);
        

            if(!theDir.exists()){
                try{
                    theDir.mkdirs();
                   
                } 

                catch(SecurityException se){
                    System.out.println(se);
                }    
               
                
            }
         
        try{
            Path targetLocation = Paths.get(rota).resolve(fileName);
            upload_dir.transferTo(targetLocation);

            System.out.println(targetLocation);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(rota)
            .path(fileName)
            .toUriString();
         
                ProdutoModelo x = new ProdutoModelo(nome,fileDownloadUri);
                ps.cadastrarAlterar(x, "cadastrar");
             
            return ResponseEntity.ok("Upload completo! Download link: " + fileDownloadUri + "  "+upload_dir);

        } catch(IOException ex){
            return ResponseEntity.ok("Upload completo! Download link: " + "  "+upload_dir);
        }
            
         
    }
    
    //DOWNLOAD
    @GetMapping("/downloads/{fileName:.+}")
    public ResponseEntity <Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws IOException{ 
        Path filepath = caminhoArquivo.resolve(fileName).normalize();

        try{
        Resource resource = new UrlResource(filepath.toUri());
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        

        if(contentType ==  null)
            contentType = "application/octet-stream";

            return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    
        }catch(MalformedInputException e){
            
        }
                return null;
    }

    //LISTAR
    @GetMapping("/listar")
    public List<ProdutoModelo> selecionar(){
        return pr.findAll();
    }

    @GetMapping("/listar/{codigo}")
        public ProdutoModelo selecionarPeloCodigo(@PathVariable Long codigo){
            return pr.findByCodigo(codigo);
        
    }

    @GetMapping("/")
    public String rota(){
        return "Funcionando...";
    }

    }

 

