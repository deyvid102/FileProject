package br.com.api.produtos.modelo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "produtos")
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file")
public class ProdutoModelo {
    

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long codigo;
    private String nome;
    private String upload_dir;

    public ProdutoModelo(String nome, String upload_dir){
        this.nome = nome;
        this.upload_dir = upload_dir;
    }
    public ProdutoModelo(){
        
    }

    public ProdutoModelo(Long codigo, String nome, String upload_dir){
        this.codigo = codigo;
        this.nome = nome;
        this.upload_dir = upload_dir;
    }
    
}
