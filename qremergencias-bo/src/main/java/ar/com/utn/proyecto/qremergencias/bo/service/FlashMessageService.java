package ar.com.utn.proyecto.qremergencias.bo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
public class FlashMessageService {

    private static final String FLASH_ERROR = "flash.error";
    private static final String FLASH_MESSAGE = "flash.message";

    @Autowired
    private MessageSource messageSource;

    public void addFlashMessage(final Model model, final String message, final String... args) {
        model.addAttribute(FLASH_MESSAGE,
                messageSource.getMessage(message, args, LocaleContextHolder.getLocale()));
    }

    public void addFlashMessage(final RedirectAttributes redirectAttributes, final String message,
                                final String... args) {
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE,
                messageSource.getMessage(message, args, LocaleContextHolder.getLocale()));
    }

    public void addFlashError(final Model model, final String message, final String... args) {
        model.addAttribute(FLASH_ERROR, messageSource.getMessage(message, args,
                LocaleContextHolder.getLocale()));
    }

    public void addFlashError(final RedirectAttributes redirectAttributes, final String message,
                              final String... args) {
        redirectAttributes.addFlashAttribute(FLASH_ERROR,
                messageSource.getMessage(message, args, LocaleContextHolder.getLocale()));
    }

}
