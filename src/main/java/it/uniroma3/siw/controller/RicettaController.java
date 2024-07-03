package it.uniroma3.siw.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import static it.uniroma3.siw.model.Credentials.CUOCO_ROLE;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.security.auth.Subject;
import static it.uniroma3.siw.model.Credentials.ADMIN_ROLE;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.Ricetta;
import it.uniroma3.siw.repository.CuocoRepository;
import it.uniroma3.siw.repository.ImageRepository;
import it.uniroma3.siw.repository.RicettaRepository;
import it.uniroma3.siw.repository.UserRepository;
import it.uniroma3.siw.service.CredentialsService;
import jakarta.validation.Valid;

@Controller
public class RicettaController {
	@Autowired private RicettaRepository ricettaRepository;
	@Autowired private CuocoRepository cuocoRepository;
	@Autowired private CredentialsService credentialsService;
	@Autowired private ImageRepository imageRepository;

	@GetMapping("/ricette")
	public String getRicette(Model model){
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if(credentials.getCuoco()!=null) {
			model.addAttribute("cuoco", credentials.getCuoco());
		}else {
			if(credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				model.addAttribute("admin",credentials.getRole());
			}else {
				model.addAttribute("user",credentials.getRole());
			}
		}
		model.addAttribute("ricette",this.ricettaRepository.findAll());
		return "ricette.html";
	}

	@GetMapping("/ricetta/{id}")
	public String getRicetta(@PathVariable Long id,Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		Ricetta r=this.ricettaRepository.findById(id).get();
		if(r.getCuoco()!=null) {
			if(credentials.getCuoco()!=null) {
				Long idProfessor=credentials.getCuoco().getId();
				if(r.getCuoco().getId()==idProfessor) {
					model.addAttribute("ricetta", this.ricettaRepository.findById(id).get());
					model.addAttribute("credentials", credentials);
					model.addAttribute("cuoco", credentials.getCuoco());
					return "ricettaEditable.html";
				}
			}
		}
		if(credentials.getRole().equals(ADMIN_ROLE)) {
			model.addAttribute("ricetta", this.ricettaRepository.findById(id).get());
			model.addAttribute("admin",credentials.getRole());
			model.addAttribute("credentials", credentials);
			return "ricettaEditable.html";
		}
		model.addAttribute("user",credentials.getRole());
		model.addAttribute("ricetta", this.ricettaRepository.findById(id).get());
		model.addAttribute("credentials", credentials);
		return "ricetta.html";
	}
	
	@GetMapping(value="/formNewRicetta")
	public String formNewRicetta(Model model) {
		model.addAttribute("ricetta", new Ricetta());
		return "formNewRicetta.html";
	}

	@PostMapping(value={"/formNewRicetta"},consumes = "multipart/form-data")
	public String newRicetta(@Valid @ModelAttribute Ricetta ricetta,  @RequestPart("file") MultipartFile file,BindingResult bindingResult, Model model) {
		UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		if(credentials.getRole().equals(CUOCO_ROLE)) {
			Cuoco c=credentials.getCuoco();
			//		this.movieValidator.validate(movie, bindingResult);
			if (!bindingResult.hasErrors()) {
				try {
					Image i=new Image();
					i.setImageData(file.getBytes());
					ricetta.setCopertina(i);
					this.imageRepository.save(i);
				} catch (Exception e) {
					System.out.println("erroreeee");
				}
				ricetta.setCuoco(c);
				this.ricettaRepository.save(ricetta);
				c.getRicette().add(ricetta);
				this.cuocoRepository.save(c); 
				return "redirect:/ricetta/"+ricetta.getId();
			} else {
				return "formNewRicetta.html"; 
			}
		}else {
			if (!bindingResult.hasErrors()) {
				try {
					Image i=new Image();
					i.setImageData(file.getBytes());

					ricetta.setCopertina(i);
					this.imageRepository.save(i);
				} catch (Exception e) {
					System.out.println("erroreeee");
				}
				this.ricettaRepository.save(ricetta); 
				return "redirect:/ricetta/"+ricetta.getId();
			}else {
				return "formNewRicetta.html"; 
			}
		}
	}



	@GetMapping(value="/setCuocoInRicetta/{idRicetta}")
	public String setCuocoInRicetta(@PathVariable Long idRicetta,Model model) {
		Ricetta ricetta=ricettaRepository.findById(idRicetta).orElse(null);
		model.addAttribute("cuochi",this.cuocoRepository.findAll());
		model.addAttribute("ricetta",ricetta);
		return "setCuocoInRicetta.html";
	}
	
	@GetMapping(value="/setCuocoInRicetta/{idRicetta}/{idCuoco}")
	public String setCuocoInRicettaPost(@PathVariable Long idRicetta,@PathVariable Long idCuoco ,Model model) {
		Ricetta ricetta=ricettaRepository.findById(idRicetta).orElse(null);
		Cuoco cuoco=cuocoRepository.findById(idCuoco).orElse(null);
		ricetta.setCuoco(cuoco);
		cuoco.getRicette().add(ricetta);
		ricettaRepository.save(ricetta);
		cuocoRepository.save(cuoco);
		return "redirect:/ricetta/"+ricetta.getId();
		
	}

	
	@GetMapping("/rimuoviRicetta/{id}")
	public String removeRicetta(@PathVariable("id") Long id, Model model) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
		Optional<Ricetta> daEliminareOptional = ricettaRepository.findById(id);

		if (daEliminareOptional.isPresent()) {
			Ricetta daEliminare = daEliminareOptional.get();
			if (credentials.getRole().equals(CUOCO_ROLE)) {
				Cuoco c = daEliminare.getCuoco();
				if (c!= null) {
					daEliminare.setCuoco(null);
					c.getRicette().remove(daEliminare);
				}
				for(Image i: imageRepository.findAll()) {
					if(i.getRicetta()!=null) {
						if(i.getRicetta().getId()==id) {
							daEliminare.getImages().remove(i);
							i.setRicetta(null);
							imageRepository.delete(i);
						}
					}
				}
				ricettaRepository.delete(daEliminare);
				cuocoRepository.save(c);
				return "redirect:/dettagliUser";
			} else {
				if (daEliminare.getCuoco()!= null) {
					Cuoco c = daEliminare.getCuoco();
					daEliminare.setCuoco(null);
					c.getRicette().remove(daEliminare);
					for(Image i: imageRepository.findAll()) {
						if(i.getRicetta()!=null) {
							if(i.getRicetta().getId()==id) {
								daEliminare.getImages().remove(i);
								i.setRicetta(null);
								imageRepository.delete(i);
							}
						}
					}
					ricettaRepository.delete(daEliminare);
					cuocoRepository.save(c);
					model.addAttribute("cuoco", c);
				}else {
					for(Image i: imageRepository.findAll()) {
						if(i.getRicetta()!=null) {
							if(i.getRicetta().getId()==id) {
								daEliminare.getImages().remove(i);
								i.setRicetta(null);
								imageRepository.delete(i);
							}
						}
					}
					ricettaRepository.delete(daEliminare);
				}
				return  "redirect:/ricette";
			}
		} else {
			return "errore.html";
		}
	}

}
