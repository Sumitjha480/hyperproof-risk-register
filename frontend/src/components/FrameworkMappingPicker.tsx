import type { RiskFrameworkFunction } from '../types/risk'

const OPTIONS: Array<{ value: RiskFrameworkFunction; label: string; description: string }> = [
 { value: 'GV', label: 'GV — Govern', description: 'Governance, policy, oversight and risk strategy' },
 { value: 'ID', label: 'ID — Identify', description: 'Understanding assets, suppliers and risk context' },
 { value: 'PR', label: 'PR — Protect', description: 'Safeguards and preventive controls' },
 { value: 'DE', label: 'DE — Detect', description: 'Monitoring and discovery of cybersecurity events' },
 { value: 'RS', label: 'RS — Respond', description: 'Actions taken when a cybersecurity event is detected' },
 { value: 'RC', label: 'RC — Recover', description: 'Restoration and resilience after an event' },
]

interface FrameworkMappingPickerProps {
 values: RiskFrameworkFunction[]
 onChange: (values: RiskFrameworkFunction[]) => void
}

export function FrameworkMappingPicker({ values, onChange }: FrameworkMappingPickerProps) {
 function toggle(value: RiskFrameworkFunction) {
   onChange(values.includes(value) ? values.filter((item) => item !== value) : [...values, value])
 }

 return (
   <div className="framework-options">
     {OPTIONS.map((option) => (
       <label className="framework-option" key={option.value}>
         <input
           type="checkbox"
           checked={values.includes(option.value)}
           onChange={() => toggle(option.value)}
         />
         <span>
           <strong>{option.label}</strong>
           <small>{option.description}</small>
         </span>
       </label>
     ))}
   </div>
 )
}
