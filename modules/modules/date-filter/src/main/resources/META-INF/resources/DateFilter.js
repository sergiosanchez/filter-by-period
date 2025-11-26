// Archivo: DateFilter.js

import {
	getCollectionFilterValue,
	setCollectionFilterValue,
} from '@liferay/fragment-renderer-collection-filter-impl';

export default function DateFilter({
	fragmentEntryLinkId,
	fragmentEntryLinkNamespace,
}) {
	const form = document.getElementById(`${fragmentEntryLinkNamespace}form`);
    
    // Cambiar a la lectura del selector
	const datePeriodSelect = form && form.elements['datePeriod']; 

	if (!form || !datePeriodSelect) {
		return;
	}

	const handleChange = () => {
        // Enviar la clave del periodo directamente
		setCollectionFilterValue(
			'date', // Clave del filtro (debe coincidir con la usada por DateInfoFilter)
			fragmentEntryLinkId,
			datePeriodSelect.value // Envía 'lastMonth', 'last3Months', etc.
		);
	};

    // Al cargar la página, intentar restaurar el valor de la URL
	const urlValue = getCollectionFilterValue('date', fragmentEntryLinkId);
    // Solo restauramos si el valor de la URL es una de nuestras claves de período
    if (['lastMonth', 'last3Months', 'lastYear'].includes(urlValue)) {
        datePeriodSelect.value = urlValue;
    } else {
        datePeriodSelect.value = '';
    }
	

	form.addEventListener('change', handleChange);

	return {
		dispose() {
			form.removeEventListener('change', handleChange);
		},
	};
}
