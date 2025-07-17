import { Button } from "@/components/ui/button";
import { FormLabel } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { cn } from "@/lib/utils";
import { AnimatePresence, motion } from "framer-motion";
import { Plus, X } from "lucide-react";
import { useEffect, useState } from "react";

interface TechnologyChoice {
    id: string;
    name: string;
  }
  
  interface TechChoicesProps {
    value: string[];
    onChange: (value: string[]) => void;
    className?: string;
  }
  
  export function TechChoices({ value, onChange, className }: TechChoicesProps) {
    const [enableLanguageSelection, setEnableLanguageSelection] = useState(false);
    const [languageOptions, setLanguageOptions] = useState<TechnologyChoice[]>([]);
    const [newTechInput, setNewTechInput] = useState("");
  
    // Parse existing value to determine if choices are enabled
    useEffect(() => {
      if (value && value.length > 0) {
        setEnableLanguageSelection(true);
        setLanguageOptions(value.map((tech, index) => ({ id: `tech-${index}`, name: tech })));
      }
    }, []);
  
    // Update parent form when choices change
    useEffect(() => {
      if (enableLanguageSelection) {
        onChange(languageOptions.map(option => option.name));
      } else {
        onChange([]);
      }
    }, [enableLanguageSelection, languageOptions, onChange]);
  
    const addTechnology = () => {
      if (newTechInput.trim() && languageOptions.length < 5) {
        const newTech: TechnologyChoice = {
          id: `tech-${Date.now()}`,
          name: newTechInput.trim()
        };
        setLanguageOptions([...languageOptions, newTech]);
        setNewTechInput("");
      }
    };
  
    const removeTechnology = (id: string) => {
      setLanguageOptions(languageOptions.filter(tech => tech.id !== id));
    };
  
    const handleKeyPress = (e: React.KeyboardEvent) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        addTechnology();
      }
    };
  
    return (
      <div className={cn("space-y-4", className)}>
        <div className="flex items-center space-x-3">
          <Switch
            id="enable-tech-choices"
            checked={enableLanguageSelection}
            onCheckedChange={setEnableLanguageSelection}
            className="data-[state=checked]:bg-violet-600"
          />
          <Label 
            htmlFor="enable-tech-choices" 
            className="text-slate-300 font-medium cursor-pointer"
          >
            Allow candidates to choose their preferred technologies
          </Label>
        </div>
  
        <AnimatePresence>
          {enableLanguageSelection && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.2 }}
              className="space-y-4"
            >
              <div className="space-y-2">
                <FormLabel className="text-slate-300 font-medium">
                  Technology Options {languageOptions.length > 0 && `(${languageOptions.length}/5)`}
                </FormLabel>
                <p className="text-sm text-slate-400">
                  Add up to 5 technology options that candidates can choose from
                </p>
              </div>
  
              <div className="flex gap-2">
                <Input
                  value={newTechInput}
                  onChange={(e) => setNewTechInput(e.target.value)}
                  onKeyDown={handleKeyPress}
                  placeholder="e.g., React, Vue, Angular..."
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                  disabled={languageOptions.length >= 5}
                />
                <Button
                  type="button"
                  onClick={addTechnology}
                  disabled={!newTechInput.trim() || languageOptions.length >= 5}
                  size="sm"
                  className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0 px-3"
                >
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
  
              <AnimatePresence>
                {languageOptions.length > 0 && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="flex flex-wrap gap-2"
                  >
                    {languageOptions.map((tech) => (
                      <motion.div
                        key={tech.id}
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.8 }}
                        transition={{ duration: 0.2 }}
                        className="flex items-center gap-2 bg-slate-800/60 border border-slate-700/50 rounded-lg px-3 py-2 text-sm text-slate-300 backdrop-blur-sm"
                      >
                        <span>{tech.name}</span>
                        <button
                          type="button"
                          onClick={() => removeTechnology(tech.id)}
                          className="text-slate-500 hover:text-red-400 transition-colors"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </motion.div>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
  
              {languageOptions.length === 0 && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className="text-center py-8 text-slate-500 text-sm"
                >
                  No technology options added yet. Add some options above.
                </motion.div>
              )}
  
              {languageOptions.length >= 5 && (
                <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className="text-center py-2 text-amber-400 text-sm"
                >
                  Maximum of 5 technology options reached
                </motion.div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    );
  }