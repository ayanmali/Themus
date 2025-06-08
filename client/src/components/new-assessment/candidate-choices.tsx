import React, { useState } from 'react';
import { ChevronDown, ChevronUp, Plus, X, Settings, Code, Users } from 'lucide-react';
import { FormLabel } from '../ui/form';

interface LanguageOption {
    id: string;
    name: string;
}

interface ChoiceConfigProps {
    onConfigChange?: (config: any) => void;
}

const ChoiceConfig: React.FC<ChoiceConfigProps> = ({ onConfigChange }) => {
    const [isLanguageSectionOpen, setIsLanguageSectionOpen] = useState(false);
    const [enableLanguageSelection, setEnableLanguageSelection] = useState(false);
    const [languageOptions, setLanguageOptions] = useState<LanguageOption[]>([
        // { id: '1', name: 'React' },
        // { id: '2', name: 'Vue' },
        // { id: '3', name: 'Angular' }
    ]);
    const [selectedLanguage, setSelectedLanguage] = useState<string>('');
    const [newLanguageName, setNewLanguageName] = useState('');
    const [isAddingNew, setIsAddingNew] = useState(false);

    const addLanguageOption = () => {
        if (newLanguageName.trim()) {
            const newOption: LanguageOption = {
                id: Date.now().toString(),
                name: newLanguageName.trim()
            };
            setLanguageOptions([...languageOptions, newOption]);
            setNewLanguageName('');
            setIsAddingNew(false);
        }
    };

    const removeLanguageOption = (id: string) => {
        setLanguageOptions(languageOptions.filter(option => option.id !== id));
        if (selectedLanguage === id) {
            setSelectedLanguage('');
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            addLanguageOption();
        } else if (e.key === 'Escape') {
            setIsAddingNew(false);
            setNewLanguageName('');
        }
    };

    return (
        <div className="space-y-8">
            {/* Language/Framework Selection Section */}
            <div className="border border-gray-700 rounded-lg bg-gray-750">
                <button
                    onClick={() => setIsLanguageSectionOpen(!isLanguageSectionOpen)}
                    className="w-full px-6 py-4 flex items-center justify-between text-left hover:bg-gray-700 transition-colors rounded-lg"
                >
                    <div className="flex-none">
                        <FormLabel className="text-slate-300 font-medium">Language and Framework Options</FormLabel>
                        <p className="text-sm text-gray-400">Allow candidates to choose their preferred technology</p>
                    </div>
                    {isLanguageSectionOpen ? (
                        <ChevronUp className="w-5 h-5 text-gray-400" />
                    ) : (
                        <ChevronDown className="w-5 h-5 text-gray-400" />
                    )}
                </button>

                {isLanguageSectionOpen && (
                    <div className="px-6 pb-6 space-y-6">
                        <div className="border-t border-gray-700 pt-6">
                            <div className="flex items-center gap-3 mb-6">
                                <input
                                    type="checkbox"
                                    id="enableLanguageSelection"
                                    checked={enableLanguageSelection}
                                    onChange={(e) => setEnableLanguageSelection(e.target.checked)}
                                    className="w-4 h-4 text-blue-600 bg-gray-700 border-gray-600 rounded focus:ring-blue-500 focus:ring-2"
                                />
                                <label htmlFor="enableLanguageSelection" className="text-white font-medium text-sm">
                                    Enable language/framework selection for candidates
                                </label>
                            </div>

                            {enableLanguageSelection && (
                                <div className="space-y-4">
                                    <div className="flex items-center justify-between">
                                        <h4 className="text-white font-medium">Available Options</h4>
                                        <button
                                            onClick={() => setIsAddingNew(true)}
                                            className="flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors text-sm font-medium"
                                        >
                                            <Plus className="w-4 h-4" />
                                            Add Option
                                        </button>
                                    </div>

                                    <div className="grid gap-3">
                                        {languageOptions.map((option) => (
                                            <div
                                                key={option.id}
                                                className="flex items-center justify-between p-4 bg-gray-700 rounded-lg border border-gray-600"
                                            >
                                                <label className="flex items-center gap-3 cursor-pointer flex-1">
                                                    {/* <input
                                                        type="radio"
                                                        name="selectedLanguage"
                                                        value={option.id}
                                                        checked={selectedLanguage === option.id}
                                                        onChange={(e) => setSelectedLanguage(e.target.value)}
                                                        className="w-4 h-4 text-blue-600 bg-gray-600 border-gray-500 focus:ring-blue-500 focus:ring-2"
                                                    /> */}
                                                    <span className="text-white font-medium">{option.name}</span>
                                                </label>
                                                <button
                                                    onClick={() => removeLanguageOption(option.id)}
                                                    className="p-1 text-gray-400 hover:text-red-400 hover:bg-red-900/20 rounded transition-colors"
                                                >
                                                    <X className="w-4 h-4" />
                                                </button>
                                            </div>
                                        ))}

                                        {isAddingNew && (
                                            <div className="flex items-center gap-3 p-4 bg-gray-700 rounded-lg border border-blue-500">
                                                <div className="w-4 h-4" /> {/* Spacer for alignment */}
                                                <input
                                                    type="text"
                                                    value={newLanguageName}
                                                    onChange={(e) => setNewLanguageName(e.target.value)}
                                                    onKeyDown={handleKeyPress}
                                                    placeholder="Enter language/framework name"
                                                    className="flex-1 px-3 py-2 bg-gray-600 border border-gray-500 rounded text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                                                    autoFocus
                                                />
                                                <div className="flex gap-2">
                                                    <button
                                                        onClick={addLanguageOption}
                                                        className="px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded text-sm font-medium transition-colors"
                                                    >
                                                        Add
                                                    </button>
                                                    <button
                                                        onClick={() => {
                                                            setIsAddingNew(false);
                                                            setNewLanguageName('');
                                                        }}
                                                        className="px-3 py-2 bg-gray-600 hover:bg-gray-500 text-white rounded text-sm font-medium transition-colors"
                                                    >
                                                        Cancel
                                                    </button>
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    {languageOptions.length === 0 && !isAddingNew && (
                                        <div className="text-center py-8 text-gray-400">
                                            <Code className="w-12 h-12 mx-auto mb-3 opacity-50" />
                                            <p className="text-sm">No language options configured</p>
                                            <p className="text-xs">Click "Add Option" to get started</p>
                                        </div>
                                    )}

                                    {languageOptions.length > 0 && (
                                        <div className="mt-6 p-4 bg-blue-900/20 border border-blue-700 rounded-lg">
                                            <div className="flex items-center gap-2 mb-2">
                                                <Users className="w-4 h-4 text-blue-400" />
                                                <span className="text-sm font-medium text-blue-300">Candidate Experience</span>
                                            </div>
                                            <p className="text-sm text-blue-200">
                                                Candidates will see these options at the start of their assessment and can choose their preferred technology to complete their tasks.
                                            </p>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {/* Action Buttons */}
            {/* <div className="flex justify-end gap-4 pt-6 border-t border-gray-700">
                <button className="px-6 py-3 border border-gray-600 text-gray-300 rounded-lg hover:bg-gray-700 transition-colors font-medium">
                    Cancel
                </button>
                <button className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors font-medium shadow-lg">
                    Save Configuration
                </button>
            </div> */}
        </div>
    );
};

export default ChoiceConfig;