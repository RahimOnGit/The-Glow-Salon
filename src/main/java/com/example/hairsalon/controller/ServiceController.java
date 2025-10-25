package com.example.hairsalon.controller;

import com.example.hairsalon.entity.Service;
import com.example.hairsalon.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/services")

public class ServiceController {


    @Autowired
    private ServiceService serviceService;
    @GetMapping("/add")
    public String tempServiceForm(Model model) {
        model.addAttribute("service", new Service());
        return "add-service";
    }


    @PostMapping("/add")
    public String addService(@Valid @ModelAttribute("service") Service service , BindingResult bindingResult, RedirectAttributes redirectAttributes , Model model) {

        if(bindingResult.hasErrors())
            return "add-service";

        try{
            if(serviceService.existsByName(service.getName()))
            {
                redirectAttributes.addFlashAttribute("service", service);
                return "redirect:/services/add";
            }

            Service savedService = serviceService.saveService(service);
            redirectAttributes.addFlashAttribute("success","Service'"+savedService.getName()+"' successfully added");
        }
        catch(Exception e)
        {
            redirectAttributes.addFlashAttribute("error","eroor adding service"+e.getMessage());
            redirectAttributes.addFlashAttribute("service",service);
            return "redirect:/admin/services/add";
        }
        return "redirect:/admin/services/add";
    }
}
